package com.codingplatform.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class StatsDtos {

    @Data
    public static class LeaderboardEntryResponse {
        private int rank;
        private Long userId;
        private String username;
        private long problemsSolved;
        private double acceptanceRate;
        private double weightedScore;

        public LeaderboardEntryResponse(int rank, Long userId, String username, long problemsSolved,
                                         double acceptanceRate, double weightedScore) {
            this.rank = rank;
            this.userId = userId;
            this.username = username;
            this.problemsSolved = problemsSolved;
            this.acceptanceRate = acceptanceRate;
            this.weightedScore = weightedScore;
        }
    }

    @Data
    public static class DashboardResponse {
        private long totalSolved;
        private Map<String, Long> solvedByDifficulty; // EASY/MEDIUM/HARD -> count
        private double acceptanceRate;
        private long totalSubmissions;
        private Map<String, Long> languageBreakdown;
        private int currentRank;
        private int currentStreak;
        private List<HeatmapDay> heatmap;
        private List<SubmissionDtos.SubmissionHistoryItem> recentSubmissions;

        @Data
        public static class HeatmapDay {
            private LocalDate date;
            private long count;

            public HeatmapDay(LocalDate date, long count) {
                this.date = date;
                this.count = count;
            }
        }
    }
}
