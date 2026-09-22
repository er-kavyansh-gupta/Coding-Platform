package com.codingplatform.service;

import com.codingplatform.dto.SubmissionDtos.*;
import com.codingplatform.entity.*;
import com.codingplatform.exception.RateLimitException;
import com.codingplatform.exception.ResourceNotFoundException;
import com.codingplatform.repository.*;
import com.codingplatform.service.execution.CodeExecutionService;
import com.codingplatform.service.execution.CompileResult;
import com.codingplatform.service.execution.ExecutionResult;
import com.codingplatform.service.execution.OutputComparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final SubmissionResultRepository submissionResultRepository;
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final UserRepository userRepository;
    private final CodeExecutionService executionService;
    private final CertificateService certificateService;
    private final ContestService contestService;

    @Value("${app.submission.rate-limit-per-minute:5}")
    private int rateLimitPerMinute;

    @Transactional
    public SubmissionResponse submit(Long userId, SubmissionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found: " + request.getProblemId()));

        Contest contest = null;
        if (request.getContestId() != null) {
            // Throws BadRequestException if the contest isn't live, the user isn't registered,
            // or this problem isn't part of the contest. Run-only ("Run" button) is still allowed
            // during a contest so competitors can test against sample cases without it counting.
            var contestProblem = contestService.validateSubmissionEligibility(request.getContestId(), problem.getId(), userId);
            contest = contestProblem.getContest();
        }

        if (!request.isRunOnly()) {
            long recent = submissionRepository.countByUserIdAndProblemIdAndSubmittedAtAfter(
                    userId, problem.getId(), LocalDateTime.now().minusMinutes(1));
            if (recent >= rateLimitPerMinute) {
                throw new RateLimitException("Rate limit exceeded: max " + rateLimitPerMinute +
                        " submissions per problem per minute. Please wait and try again.");
            }
        }

        List<TestCase> testCases = request.isRunOnly()
                ? testCaseRepository.findByProblemIdAndIsHiddenFalseOrderByDisplayOrderAsc(problem.getId())
                : testCaseRepository.findByProblemIdOrderByDisplayOrderAsc(problem.getId());

        Submission submission = Submission.builder()
                .user(user)
                .problem(problem)
                .contest(contest)
                .language(request.getLanguage())
                .sourceCode(request.getSourceCode())
                .isRunOnly(request.isRunOnly())
                .status(SubmissionStatus.RUNNING)
                .build();
        submission = submissionRepository.save(submission);

        Path workDir = null;
        try {
            workDir = executionService.createWorkDir();
            executionService.writeSource(workDir, request.getLanguage(), request.getSourceCode());

            CompileResult compileResult = executionService.compile(workDir, request.getLanguage());
            if (!compileResult.isSuccess()) {
                submission.setStatus(SubmissionStatus.COMPILATION_ERROR);
                submission.setCompileError(compileResult.getErrorOutput());
                submissionRepository.save(submission);
                return toResponse(submission, List.of());
            }

            List<SubmissionResult> results = new ArrayList<>();
            long totalTimeMs = 0;
            long maxMemoryKb = 0;
            SubmissionStatus overall = SubmissionStatus.ACCEPTED;

            for (TestCase tc : testCases) {
                Integer timeLimit = tc.getTimeLimitOverrideMs() != null ? tc.getTimeLimitOverrideMs() : problem.getTimeLimitMs();
                ExecutionResult execResult = executionService.run(
                        workDir, request.getLanguage(), tc.getInput(), timeLimit, problem.getMemoryLimitMb());

                SubmissionStatus verdict;
                boolean passed;
                if (execResult.isTimedOut()) {
                    verdict = SubmissionStatus.TIME_LIMIT_EXCEEDED;
                    passed = false;
                } else if (execResult.getExitCode() != 0) {
                    verdict = SubmissionStatus.RUNTIME_ERROR;
                    passed = false;
                } else if (OutputComparator.matches(execResult.getStdout(), tc.getExpectedOutput())) {
                    verdict = SubmissionStatus.ACCEPTED;
                    passed = true;
                } else {
                    verdict = SubmissionStatus.WRONG_ANSWER;
                    passed = false;
                }

                totalTimeMs += execResult.getExecutionTimeMs();
                maxMemoryKb = Math.max(maxMemoryKb, execResult.getMemoryKb());

                SubmissionResult sr = SubmissionResult.builder()
                        .submission(submission)
                        .testCase(tc)
                        .passed(passed)
                        .verdict(verdict)
                        .actualOutput(execResult.getStdout())
                        .stderr(execResult.getStderr())
                        .executionTimeMs(execResult.getExecutionTimeMs())
                        .memoryKb(execResult.getMemoryKb())
                        .isHidden(tc.getIsHidden())
                        .build();
                results.add(submissionResultRepository.save(sr));

                if (!passed && overall == SubmissionStatus.ACCEPTED) {
                    overall = verdict; // first failing verdict becomes the overall verdict
                }
            }

            submission.setStatus(testCases.isEmpty() ? SubmissionStatus.ACCEPTED : overall);
            submission.setTotalExecutionTimeMs(totalTimeMs);
            submission.setMaxMemoryKb(maxMemoryKb);
            submissionRepository.save(submission);

            if (!request.isRunOnly() && overall == SubmissionStatus.ACCEPTED) {
                updateStreak(user);
                certificateService.checkAndIssue(user);
            }

            return toResponse(submission, results);

        } catch (Exception e) {
            log.error("Execution failed for submission {}", submission.getId(), e);
            submission.setStatus(SubmissionStatus.RUNTIME_ERROR);
            submission.setCompileError("Internal execution error: " + e.getMessage());
            submissionRepository.save(submission);
            return toResponse(submission, List.of());
        } finally {
            executionService.cleanupWorkDir(workDir);
        }
    }

    private void updateStreak(User user) {
        LocalDate today = LocalDate.now();
        LocalDate last = user.getLastSolvedDate();
        if (last == null || last.isBefore(today.minusDays(1))) {
            user.setCurrentStreak(1);
        } else if (last.equals(today.minusDays(1))) {
            user.setCurrentStreak((user.getCurrentStreak() == null ? 0 : user.getCurrentStreak()) + 1);
        } // else: already solved today, streak unchanged
        user.setLastSolvedDate(today);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<SubmissionHistoryItem> history(Long userId, Long problemId) {
        List<Submission> submissions = problemId != null
                ? submissionRepository.findByUserIdAndProblemIdOrderBySubmittedAtDesc(userId, problemId)
                : submissionRepository.findByUserIdOrderBySubmittedAtDesc(userId);

        List<SubmissionHistoryItem> out = new ArrayList<>();
        for (Submission s : submissions) {
            SubmissionHistoryItem item = new SubmissionHistoryItem();
            item.setSubmissionId(s.getId());
            item.setProblemId(s.getProblem().getId());
            item.setProblemTitle(s.getProblem().getTitle());
            item.setLanguage(s.getLanguage());
            item.setStatus(s.getStatus());
            item.setSubmittedAt(s.getSubmittedAt());
            out.add(item);
        }
        return out;
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getSubmission(Long submissionId, Long requestingUserId, boolean isAdmin) {
        Submission s = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found: " + submissionId));
        if (!isAdmin && !s.getUser().getId().equals(requestingUserId)) {
            throw new ResourceNotFoundException("Submission not found: " + submissionId);
        }
        List<SubmissionResult> results = submissionResultRepository.findBySubmissionIdOrderByIdAsc(submissionId);
        return toResponse(s, results);
    }

    private SubmissionResponse toResponse(Submission submission, List<SubmissionResult> results) {
        SubmissionResponse res = new SubmissionResponse();
        res.setSubmissionId(submission.getId());
        res.setOverallStatus(submission.getStatus());
        res.setCompileError(submission.getCompileError());
        res.setTotalExecutionTimeMs(submission.getTotalExecutionTimeMs());
        res.setMaxMemoryKb(submission.getMaxMemoryKb());
        res.setSubmittedAt(submission.getSubmittedAt());

        List<TestCaseResultResponse> resultDtos = new ArrayList<>();
        int passed = 0;
        for (SubmissionResult r : results) {
            TestCaseResultResponse dto = new TestCaseResultResponse();
            dto.setTestCaseId(r.getTestCase().getId());
            dto.setHidden(r.getIsHidden());
            dto.setPassed(r.getPassed());
            dto.setVerdict(r.getVerdict());
            // Hide input/expected/actual output for hidden test cases so answers aren't leaked
            if (Boolean.FALSE.equals(r.getIsHidden())) {
                dto.setInput(r.getTestCase().getInput());
                dto.setExpectedOutput(r.getTestCase().getExpectedOutput());
                dto.setActualOutput(r.getActualOutput());
            }
            dto.setStderr(r.getStderr());
            dto.setExecutionTimeMs(r.getExecutionTimeMs());
            dto.setMemoryKb(r.getMemoryKb());
            if (Boolean.TRUE.equals(r.getPassed())) passed++;
            resultDtos.add(dto);
        }
        res.setResults(resultDtos);
        res.setPassedCount(passed);
        res.setTotalCount(results.size());
        return res;
    }
}
