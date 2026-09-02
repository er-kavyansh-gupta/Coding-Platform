package com.codingplatform.service;

import com.codingplatform.dto.StatsDtos.DashboardResponse;
import com.codingplatform.dto.SubmissionDtos.SubmissionHistoryItem;
import com.codingplatform.entity.Submission;
import com.codingplatform.entity.SubmissionStatus;
import com.codingplatform.entity.User;
import com.codingplatform.exception.ResourceNotFoundException;
import com.codingplatform.repository.SubmissionRepository;
import com.codingplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final LeaderboardService leaderboardService;
    private static final int HEATMAP_DAYS = 365;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Submission> allSubmissions = submissionRepository.findByUserIdOrderBySubmittedAtDesc(userId).stream()
                .filter(s -> !s.getIsRunOnly()).toList();

        Set<Long> solvedIds = new HashSet<>();
        Map<String, Long> byDifficulty = new LinkedHashMap<>(Map.of("EASY", 0L, "MEDIUM", 0L, "HARD", 0L));
        for (Submission s : allSubmissions) {
            if (s.getStatus() == SubmissionStatus.ACCEPTED && solvedIds.add(s.getProblem().getId())) {
                String diff = s.getProblem().getDifficulty().name();
                byDifficulty.merge(diff, 1L, Long::sum);
            }
        }

        long total = allSubmissions.size();
        long accepted = allSubmissions.stream().filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED).count();
        double acceptanceRate = total == 0 ? 0 : Math.round((accepted * 1000.0) / total) / 10.0;

        Map<String, Long> languageBreakdown = allSubmissions.stream()
                .collect(Collectors.groupingBy(s -> s.getLanguage().name(), Collectors.counting()));

        LocalDateTime since = LocalDateTime.now().minusDays(HEATMAP_DAYS);
        List<Submission> heatmapSubs = submissionRepository.findHeatmapSubmissions(userId, since);
        Map<java.time.LocalDate, Long> counts = heatmapSubs.stream()
                .collect(Collectors.groupingBy(s -> s.getSubmittedAt().toLocalDate(), Collectors.counting()));
        List<DashboardResponse.HeatmapDay> heatmap = counts.entrySet().stream()
                .map(e -> new DashboardResponse.HeatmapDay(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(DashboardResponse.HeatmapDay::getDate))
                .toList();

        List<SubmissionHistoryItem> recent = allSubmissions.stream().limit(10).map(s -> {
            SubmissionHistoryItem item = new SubmissionHistoryItem();
            item.setSubmissionId(s.getId());
            item.setProblemId(s.getProblem().getId());
            item.setProblemTitle(s.getProblem().getTitle());
            item.setLanguage(s.getLanguage());
            item.setStatus(s.getStatus());
            item.setSubmittedAt(s.getSubmittedAt());
            return item;
        }).toList();

        DashboardResponse res = new DashboardResponse();
        res.setTotalSolved(solvedIds.size());
        res.setSolvedByDifficulty(byDifficulty);
        res.setAcceptanceRate(acceptanceRate);
        res.setTotalSubmissions(total);
        res.setLanguageBreakdown(languageBreakdown);
        res.setCurrentRank(leaderboardService.getUserRank(userId));
        res.setCurrentStreak(user.getCurrentStreak() == null ? 0 : user.getCurrentStreak());
        res.setHeatmap(heatmap);
        res.setRecentSubmissions(recent);
        return res;
    }
}
