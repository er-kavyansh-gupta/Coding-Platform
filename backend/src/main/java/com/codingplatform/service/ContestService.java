package com.codingplatform.service;

import com.codingplatform.dto.ContestDtos.*;
import com.codingplatform.entity.*;
import com.codingplatform.exception.BadRequestException;
import com.codingplatform.exception.ResourceNotFoundException;
import com.codingplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ContestService {

    private final ContestRepository contestRepository;
    private final ContestProblemRepository contestProblemRepository;
    private final ContestRegistrationRepository registrationRepository;
    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    private static final int WRONG_ATTEMPT_PENALTY_MINUTES = 10;

    // -----------------------------------------------------------------
    // Admin: create / update / delete
    // -----------------------------------------------------------------

    @Transactional
    public Contest create(Long adminUserId, ContestRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Contest contest = Contest.builder()
                .title(request.getTitle())
                .organizationName(request.getOrganizationName())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isPublished(Boolean.TRUE.equals(request.getIsPublished()))
                .createdBy(admin)
                .build();
        contest = contestRepository.save(contest);
        applyProblems(contest, request.getProblems());
        return contest;
    }

    @Transactional
    public Contest update(Long contestId, ContestRequest request) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found: " + contestId));
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }
        contest.setTitle(request.getTitle());
        contest.setOrganizationName(request.getOrganizationName());
        contest.setDescription(request.getDescription());
        contest.setStartTime(request.getStartTime());
        contest.setEndTime(request.getEndTime());
        contest.setIsPublished(Boolean.TRUE.equals(request.getIsPublished()));

        if (request.getProblems() != null) {
            contestProblemRepository.deleteAll(contestProblemRepository.findByContestIdOrderByDisplayOrderAsc(contestId));
            applyProblems(contest, request.getProblems());
        }
        return contestRepository.save(contest);
    }

    @Transactional
    public void delete(Long contestId) {
        if (!contestRepository.existsById(contestId)) {
            throw new ResourceNotFoundException("Contest not found: " + contestId);
        }
        contestRepository.deleteById(contestId);
    }

    private void applyProblems(Contest contest, List<ContestProblemRequest> problems) {
        if (problems == null) return;
        int order = 0;
        for (ContestProblemRequest pr : problems) {
            Problem problem = problemRepository.findById(pr.getProblemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Problem not found: " + pr.getProblemId()));
            ContestProblem cp = ContestProblem.builder()
                    .contest(contest)
                    .problem(problem)
                    .points(pr.getPoints() != null ? pr.getPoints() : 100)
                    .displayOrder(pr.getDisplayOrder() != null ? pr.getDisplayOrder() : order)
                    .build();
            contestProblemRepository.save(cp);
            order++;
        }
    }

    // -----------------------------------------------------------------
    // Browsing
    // -----------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ContestSummaryResponse> list(Long currentUserId, boolean includeUnpublished) {
        List<Contest> contests = includeUnpublished
                ? contestRepository.findAllByOrderByStartTimeDesc()
                : contestRepository.findByIsPublishedTrueOrderByStartTimeDesc();

        List<ContestSummaryResponse> out = new ArrayList<>();
        for (Contest c : contests) {
            ContestSummaryResponse dto = new ContestSummaryResponse();
            dto.setId(c.getId());
            dto.setTitle(c.getTitle());
            dto.setOrganizationName(c.getOrganizationName());
            dto.setStatus(c.computeStatus());
            dto.setStartTime(c.getStartTime());
            dto.setEndTime(c.getEndTime());
            dto.setRegistered(currentUserId != null && registrationRepository.existsByContestIdAndUserId(c.getId(), currentUserId));
            dto.setParticipantCount(registrationRepository.countByContestId(c.getId()));
            dto.setProblemCount(contestProblemRepository.findByContestIdOrderByDisplayOrderAsc(c.getId()).size());
            dto.setIsPublished(c.getIsPublished());
            out.add(dto);
        }
        return out;
    }

    @Transactional(readOnly = true)
    public ContestDetailResponse getDetail(Long contestId, Long currentUserId, boolean isAdmin) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found: " + contestId));

        boolean registered = currentUserId != null && registrationRepository.existsByContestIdAndUserId(contestId, currentUserId);
        ContestStatus status = contest.computeStatus();

        ContestDetailResponse dto = new ContestDetailResponse();
        dto.setId(contest.getId());
        dto.setTitle(contest.getTitle());
        dto.setOrganizationName(contest.getOrganizationName());
        dto.setDescription(contest.getDescription());
        dto.setStatus(status);
        dto.setStartTime(contest.getStartTime());
        dto.setEndTime(contest.getEndTime());
        dto.setRegistered(registered);
        dto.setParticipantCount(registrationRepository.countByContestId(contestId));
        dto.setIsPublished(contest.getIsPublished());

        List<ContestProblemSummary> problems = new ArrayList<>();
        // Problem statements/points are only revealed once the contest has started (and only to registered
        // users), so an upcoming contest doesn't leak its questions ahead of time. Admins always see them
        // (needed to edit the contest before it goes live).
        boolean canSeeProblems = isAdmin || (status != ContestStatus.UPCOMING && (registered || currentUserId == null));
        if (canSeeProblems) {
            for (ContestProblem cp : contestProblemRepository.findByContestIdOrderByDisplayOrderAsc(contestId)) {
                ContestProblemSummary ps = new ContestProblemSummary();
                ps.setProblemId(cp.getProblem().getId());
                ps.setTitle(cp.getProblem().getTitle());
                ps.setDifficulty(cp.getProblem().getDifficulty());
                ps.setPoints(cp.getPoints());
                ps.setSolvedByUser(currentUserId != null && hasFirstAccepted(contestId, currentUserId, cp.getProblem().getId()));
                problems.add(ps);
            }
        }
        dto.setProblems(problems);
        return dto;
    }

    @Transactional
    public void register(Long contestId, Long userId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found: " + contestId));
        if (contest.computeStatus() == ContestStatus.ENDED) {
            throw new BadRequestException("This contest has already ended");
        }
        if (registrationRepository.existsByContestIdAndUserId(contestId, userId)) {
            return; // already registered, no-op
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        registrationRepository.save(ContestRegistration.builder().contest(contest).user(user).build());
    }

    /** Used by SubmissionService to validate a contest submission before running it. */
    @Transactional(readOnly = true)
    public ContestProblem validateSubmissionEligibility(Long contestId, Long problemId, Long userId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found: " + contestId));
        if (contest.computeStatus() != ContestStatus.ONGOING) {
            throw new BadRequestException("This contest is not currently active");
        }
        if (!registrationRepository.existsByContestIdAndUserId(contestId, userId)) {
            throw new BadRequestException("You must register for this contest before submitting");
        }
        return contestProblemRepository.findByContestIdAndProblemId(contestId, problemId)
                .orElseThrow(() -> new BadRequestException("This problem is not part of the contest"));
    }

    // -----------------------------------------------------------------
    // Leaderboard
    // -----------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ContestLeaderboardEntryResponse> getLeaderboard(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found: " + contestId));
        List<ContestProblem> contestProblems = contestProblemRepository.findByContestIdOrderByDisplayOrderAsc(contestId);
        Map<Long, Integer> pointsByProblem = new HashMap<>();
        for (ContestProblem cp : contestProblems) pointsByProblem.put(cp.getProblem().getId(), cp.getPoints());

        List<Submission> all = submissionRepository.findByContestIdOrderBySubmittedAtAsc(contestId);

        // userId -> problemId -> list of submissions (chronological)
        Map<Long, Map<Long, List<Submission>>> byUserAndProblem = new HashMap<>();
        Map<Long, User> usersById = new HashMap<>();
        for (Submission s : all) {
            Long userId = s.getUser().getId();
            usersById.put(userId, s.getUser());
            byUserAndProblem
                    .computeIfAbsent(userId, k -> new HashMap<>())
                    .computeIfAbsent(s.getProblem().getId(), k -> new ArrayList<>())
                    .add(s);
        }

        List<ContestLeaderboardEntryResponse> entries = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, List<Submission>>> userEntry : byUserAndProblem.entrySet()) {
            Long userId = userEntry.getKey();
            int totalPoints = 0;
            long totalPenalty = 0;
            int solvedCount = 0;

            for (Map.Entry<Long, List<Submission>> probEntry : userEntry.getValue().entrySet()) {
                Long problemId = probEntry.getKey();
                List<Submission> subs = probEntry.getValue(); // chronological
                int wrongBeforeAc = 0;
                Submission firstAccepted = null;
                for (Submission s : subs) {
                    if (s.getStatus() == SubmissionStatus.ACCEPTED) {
                        firstAccepted = s;
                        break;
                    }
                    wrongBeforeAc++;
                }
                if (firstAccepted != null) {
                    solvedCount++;
                    totalPoints += pointsByProblem.getOrDefault(problemId, 0);
                    long minutesFromStart = Duration.between(contest.getStartTime(), firstAccepted.getSubmittedAt()).toMinutes();
                    totalPenalty += Math.max(0, minutesFromStart) + (long) wrongBeforeAc * WRONG_ATTEMPT_PENALTY_MINUTES;
                }
            }

            if (solvedCount > 0) {
                User u = usersById.get(userId);
                entries.add(new ContestLeaderboardEntryResponse(0, userId, u.getUsername(), totalPoints, totalPenalty, solvedCount));
            }
        }

        entries.sort(Comparator.comparingInt(ContestLeaderboardEntryResponse::getTotalPoints).reversed()
                .thenComparingLong(ContestLeaderboardEntryResponse::getTotalPenaltyMinutes));

        int rank = 1;
        for (ContestLeaderboardEntryResponse e : entries) {
            e.setRank(rank++);
        }
        return entries;
    }

    private boolean hasFirstAccepted(Long contestId, Long userId, Long problemId) {
        return submissionRepository.findByContestIdAndUserIdAndProblemIdOrderBySubmittedAtAsc(contestId, userId, problemId)
                .stream().anyMatch(s -> s.getStatus() == SubmissionStatus.ACCEPTED);
    }
}
