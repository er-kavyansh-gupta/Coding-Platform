package com.codingplatform.service;

import com.codingplatform.dto.StatsDtos.LeaderboardEntryResponse;
import com.codingplatform.entity.*;
import com.codingplatform.repository.SubmissionRepository;
import com.codingplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;

    public enum Scope { GLOBAL, WEEKLY }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getLeaderboard(Scope scope, Language languageFilter, int limit) {
        List<User> users = userRepository.findAll();
        LocalDateTime since = scope == Scope.WEEKLY ? LocalDateTime.now().minusDays(7) : null;

        List<LeaderboardEntryResponse> entries = new ArrayList<>();
        for (User u : users) {
            List<Submission> accepted = submissionRepository.findByUserIdOrderBySubmittedAtDesc(u.getId()).stream()
                    .filter(s -> !s.getIsRunOnly())
                    .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED)
                    .filter(s -> since == null || s.getSubmittedAt().isAfter(since))
                    .filter(s -> languageFilter == null || s.getLanguage() == languageFilter)
                    .toList();

            Set<Long> solvedProblemIds = new HashSet<>();
            double weighted = 0;
            for (Submission s : accepted) {
                if (solvedProblemIds.add(s.getProblem().getId())) {
                    weighted += s.getProblem().getDifficulty().getWeight();
                }
            }

            long totalSubs = submissionRepository.countTotalSubmissions(u.getId());
            long acceptedSubs = submissionRepository.countAcceptedSubmissions(u.getId());
            double accuracy = totalSubs == 0 ? 0 : (acceptedSubs * 100.0) / totalSubs;

            if (solvedProblemIds.isEmpty() && scope == Scope.WEEKLY) continue; // skip inactive users in weekly view

            entries.add(new LeaderboardEntryResponse(0, u.getId(), u.getUsername(),
                    solvedProblemIds.size(), Math.round(accuracy * 10) / 10.0, weighted));
        }

        entries.sort(Comparator.comparingDouble(LeaderboardEntryResponse::getWeightedScore).reversed()
                .thenComparing(Comparator.comparingDouble(LeaderboardEntryResponse::getAcceptanceRate).reversed()));

        List<LeaderboardEntryResponse> ranked = new ArrayList<>();
        int rank = 1;
        for (LeaderboardEntryResponse e : entries) {
            e.setRank(rank++);
            ranked.add(e);
            if (ranked.size() >= limit) break;
        }
        return ranked;
    }

    @Transactional(readOnly = true)
    public int getUserRank(Long userId) {
        List<LeaderboardEntryResponse> full = getLeaderboard(Scope.GLOBAL, null, Integer.MAX_VALUE);
        return full.stream().filter(e -> e.getUserId().equals(userId)).findFirst()
                .map(LeaderboardEntryResponse::getRank).orElse(-1);
    }
}
