package com.codingplatform.dto;

import com.codingplatform.entity.CertificateTier;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class CertificateDtos {

    @Data
    public static class CertificateResponse {
        private CertificateTier tier;
        private Integer problemsSolvedAtIssue;
        private String serialCode;
        private LocalDateTime issuedAt;

        public CertificateResponse(CertificateTier tier, Integer problemsSolvedAtIssue, String serialCode, LocalDateTime issuedAt) {
            this.tier = tier;
            this.problemsSolvedAtIssue = problemsSolvedAtIssue;
            this.serialCode = serialCode;
            this.issuedAt = issuedAt;
        }
    }

    @Data
    public static class CertificateSummaryResponse {
        private long totalSolved;
        private List<CertificateResponse> earned;
        private CertificateTier nextTier; // null if all tiers earned
        private Integer problemsUntilNextTier; // null if all tiers earned
        private String username;
    }
}
