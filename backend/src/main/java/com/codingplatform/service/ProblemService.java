package com.codingplatform.service;

import com.codingplatform.dto.ProblemDtos.*;
import com.codingplatform.entity.*;
import com.codingplatform.exception.ResourceNotFoundException;
import com.codingplatform.repository.ProblemRepository;
import com.codingplatform.repository.SubmissionRepository;
import com.codingplatform.repository.TestCaseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final SubmissionRepository submissionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public List<ProblemSummaryResponse> list(Difficulty difficulty, String tag, String search, Long currentUserId) {
        List<Problem> problems = problemRepository.search(difficulty, tag, search);
        List<ProblemSummaryResponse> out = new ArrayList<>();
        for (Problem p : problems) {
            long total = submissionRepository.countByProblemIdAndIsRunOnlyFalse(p.getId());
            long accepted = submissionRepository.countByProblemIdAndStatusAndIsRunOnlyFalse(p.getId(), SubmissionStatus.ACCEPTED);
            double rate = total == 0 ? 0.0 : (accepted * 100.0) / total;
            boolean solved = currentUserId != null &&
                    submissionRepository.existsByUserIdAndProblemIdAndStatusAndIsRunOnlyFalse(currentUserId, p.getId(), SubmissionStatus.ACCEPTED);
            out.add(new ProblemSummaryResponse(p.getId(), p.getTitle(), p.getDifficulty(), p.getTags(), solved, Math.round(rate * 10) / 10.0));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public ProblemDetailResponse getDetail(Long problemId, Long currentUserId) {
        Problem p = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found: " + problemId));

        ProblemDetailResponse res = new ProblemDetailResponse();
        res.setId(p.getId());
        res.setTitle(p.getTitle());
        res.setDescription(p.getDescription());
        res.setDifficulty(p.getDifficulty());
        res.setTags(p.getTags());
        res.setStarterCodeJava(p.getStarterCodeJava());
        res.setStarterCodePython(p.getStarterCodePython());
        res.setStarterCodeCpp(p.getStarterCodeCpp());
        res.setStarterCodeJavascript(p.getStarterCodeJavascript());
        res.setTimeLimitMs(p.getTimeLimitMs());
        res.setMemoryLimitMb(p.getMemoryLimitMb());

        List<ProblemDetailResponse.SampleTestCase> samples = testCaseRepository
                .findByProblemIdAndIsHiddenFalseOrderByDisplayOrderAsc(problemId)
                .stream()
                .map(tc -> new ProblemDetailResponse.SampleTestCase(tc.getInput(), tc.getExpectedOutput()))
                .toList();
        res.setSampleTestCases(samples);

        res.setHints(parseHints(p.getHintsJson()));

        boolean unlocked = false;
        if (currentUserId != null && p.getUnlockEditorialAfterFailures() != null) {
            long failedAttempts = submissionRepository.countByUserIdAndProblemIdAndIsRunOnlyFalseAndStatusNot(
                    currentUserId, problemId, SubmissionStatus.ACCEPTED);
            boolean solved = submissionRepository.existsByUserIdAndProblemIdAndStatusAndIsRunOnlyFalse(
                    currentUserId, problemId, SubmissionStatus.ACCEPTED);
            unlocked = solved || failedAttempts >= p.getUnlockEditorialAfterFailures();
        }
        res.setEditorialUnlocked(unlocked);
        res.setEditorial(unlocked ? p.getEditorial() : null);

        return res;
    }

    @Transactional
    public Problem create(ProblemRequest request) {
        Problem problem = Problem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .difficulty(request.getDifficulty())
                .tags(request.getTags() != null ? request.getTags() : Collections.emptyList())
                .hintsJson(toJson(request.getHints()))
                .editorial(request.getEditorial())
                .unlockEditorialAfterFailures(request.getUnlockEditorialAfterFailures())
                .starterCodeJava(request.getStarterCodeJava())
                .starterCodePython(request.getStarterCodePython())
                .starterCodeCpp(request.getStarterCodeCpp())
                .starterCodeJavascript(request.getStarterCodeJavascript())
                .timeLimitMs(request.getTimeLimitMs())
                .memoryLimitMb(request.getMemoryLimitMb())
                .build();

        problem = problemRepository.save(problem);
        applyTestCases(problem, request.getTestCases());
        return problem;
    }

    @Transactional
    public Problem update(Long problemId, ProblemRequest request) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found: " + problemId));

        problem.setTitle(request.getTitle());
        problem.setDescription(request.getDescription());
        problem.setDifficulty(request.getDifficulty());
        problem.setTags(request.getTags() != null ? request.getTags() : Collections.emptyList());
        problem.setHintsJson(toJson(request.getHints()));
        problem.setEditorial(request.getEditorial());
        problem.setUnlockEditorialAfterFailures(request.getUnlockEditorialAfterFailures());
        problem.setStarterCodeJava(request.getStarterCodeJava());
        problem.setStarterCodePython(request.getStarterCodePython());
        problem.setStarterCodeCpp(request.getStarterCodeCpp());
        problem.setStarterCodeJavascript(request.getStarterCodeJavascript());
        problem.setTimeLimitMs(request.getTimeLimitMs());
        problem.setMemoryLimitMb(request.getMemoryLimitMb());

        if (request.getTestCases() != null) {
            testCaseRepository.deleteAll(testCaseRepository.findByProblemIdOrderByDisplayOrderAsc(problemId));
            applyTestCases(problem, request.getTestCases());
        }

        return problemRepository.save(problem);
    }

    @Transactional
    public void delete(Long problemId) {
        if (!problemRepository.existsById(problemId)) {
            throw new ResourceNotFoundException("Problem not found: " + problemId);
        }
        problemRepository.deleteById(problemId);
    }

    private void applyTestCases(Problem problem, List<TestCaseRequest> testCases) {
        if (testCases == null) return;
        int order = 0;
        for (TestCaseRequest tcr : testCases) {
            TestCase tc = TestCase.builder()
                    .problem(problem)
                    .input(tcr.getInput())
                    .expectedOutput(tcr.getExpectedOutput())
                    .isHidden(tcr.getIsHidden() != null ? tcr.getIsHidden() : true)
                    .timeLimitOverrideMs(tcr.getTimeLimitOverrideMs())
                    .displayOrder(tcr.getDisplayOrder() != null ? tcr.getDisplayOrder() : order)
                    .build();
            testCaseRepository.save(tc);
            order++;
        }
    }

    private String toJson(List<String> list) {
        if (list == null) return "[]";
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> parseHints(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
