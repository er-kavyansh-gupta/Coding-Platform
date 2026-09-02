package com.codingplatform.dto;

import com.codingplatform.entity.Language;
import com.codingplatform.entity.SubmissionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class SubmissionDtos {

    @Data
    public static class SubmissionRequest {
        @NotNull
        private Long problemId;
        @NotNull
        private Language language;
        @NotBlank
        private String sourceCode;
        private boolean runOnly = false; // true = "Run" (sample cases only)
    }

    @Data
    public static class TestCaseResultResponse {
        private Long testCaseId;
        private boolean hidden;
        private boolean passed;
        private SubmissionStatus verdict;
        private String input;
        private String expectedOutput;
        private String actualOutput;
        private String stderr;
        private Long executionTimeMs;
        private Long memoryKb;
    }

    @Data
    public static class SubmissionResponse {
        private Long submissionId;
        private SubmissionStatus overallStatus;
        private String compileError;
        private Long totalExecutionTimeMs;
        private Long maxMemoryKb;
        private int passedCount;
        private int totalCount;
        private List<TestCaseResultResponse> results;
        private LocalDateTime submittedAt;
    }

    @Data
    public static class SubmissionHistoryItem {
        private Long submissionId;
        private Long problemId;
        private String problemTitle;
        private Language language;
        private SubmissionStatus status;
        private LocalDateTime submittedAt;
    }
}
