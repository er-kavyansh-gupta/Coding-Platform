package com.codingplatform.dto;

import com.codingplatform.entity.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

public class ProblemDtos {

    @Data
    public static class TestCaseRequest {
        private Long id; // null for new
        @NotBlank
        private String input;
        @NotBlank
        private String expectedOutput;
        private Boolean isHidden = true;
        private Integer timeLimitOverrideMs;
        private Integer displayOrder = 0;
    }

    @Data
    public static class ProblemRequest {
        @NotBlank
        private String title;
        @NotBlank
        private String description;
        @NotNull
        private Difficulty difficulty;
        private List<String> tags;
        private List<String> hints;
        private String editorial;
        private Integer unlockEditorialAfterFailures = 3;
        private String starterCodeJava;
        private String starterCodePython;
        private String starterCodeCpp;
        private String starterCodeJavascript;
        private Integer timeLimitMs = 2000;
        private Integer memoryLimitMb = 256;
        private List<TestCaseRequest> testCases;
    }

    @Data
    public static class ProblemSummaryResponse {
        private Long id;
        private String title;
        private Difficulty difficulty;
        private List<String> tags;
        private boolean solvedByUser;
        private double acceptanceRate;

        public ProblemSummaryResponse(Long id, String title, Difficulty difficulty, List<String> tags,
                                       boolean solvedByUser, double acceptanceRate) {
            this.id = id;
            this.title = title;
            this.difficulty = difficulty;
            this.tags = tags;
            this.solvedByUser = solvedByUser;
            this.acceptanceRate = acceptanceRate;
        }
    }

    @Data
    public static class ProblemDetailResponse {
        private Long id;
        private String title;
        private String description;
        private Difficulty difficulty;
        private List<String> tags;
        private String starterCodeJava;
        private String starterCodePython;
        private String starterCodeCpp;
        private String starterCodeJavascript;
        private Integer timeLimitMs;
        private Integer memoryLimitMb;
        private List<SampleTestCase> sampleTestCases;
        private boolean editorialUnlocked;
        private String editorial; // only populated if unlocked
        private List<String> hints;

        @Data
        public static class SampleTestCase {
            private String input;
            private String expectedOutput;

            public SampleTestCase(String input, String expectedOutput) {
                this.input = input;
                this.expectedOutput = expectedOutput;
            }
        }
    }
}
