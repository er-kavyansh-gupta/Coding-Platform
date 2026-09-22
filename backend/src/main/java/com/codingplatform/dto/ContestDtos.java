package com.codingplatform.dto;

import com.codingplatform.entity.ContestStatus;
import com.codingplatform.entity.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class ContestDtos {

    @Data
    public static class ContestProblemRequest {
        @NotNull
        private Long problemId;
        private Integer points = 100;
        private Integer displayOrder = 0;
    }

    @Data
    public static class ContestRequest {
        @NotBlank
        private String title;
        @NotBlank
        private String organizationName;
        private String description;
        @NotNull
        private LocalDateTime startTime;
        @NotNull
        private LocalDateTime endTime;
        private Boolean isPublished = false;
        private List<ContestProblemRequest> problems;
    }

    @Data
    public static class ContestSummaryResponse {
        private Long id;
        private String title;
        private String organizationName;
        private ContestStatus status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private boolean registered;
        private long participantCount;
        private int problemCount;
        private Boolean isPublished;
    }

    @Data
    public static class ContestProblemSummary {
        private Long problemId;
        private String title;
        private Difficulty difficulty;
        private Integer points;
        private boolean solvedByUser;
    }

    @Data
    public static class ContestDetailResponse {
        private Long id;
        private String title;
        private String organizationName;
        private String description;
        private ContestStatus status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private boolean registered;
        private long participantCount;
        private List<ContestProblemSummary> problems;
        private Boolean isPublished;
    }
    @Data
    public static class ContestLeaderboardEntryResponse {
        private int rank;
        private Long userId;
        private String username;
        private int totalPoints;
        private long totalPenaltyMinutes;
        private int problemsSolved;

        public ContestLeaderboardEntryResponse(int rank, Long userId, String username, int totalPoints,
                                                long totalPenaltyMinutes, int problemsSolved) {
            this.rank = rank;
            this.userId = userId;
            this.username = username;
            this.totalPoints = totalPoints;
            this.totalPenaltyMinutes = totalPenaltyMinutes;
            this.problemsSolved = problemsSolved;
        }
    }
}
