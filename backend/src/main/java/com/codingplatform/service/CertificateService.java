package com.codingplatform.service;

import com.codingplatform.dto.CertificateDtos.CertificateResponse;
import com.codingplatform.dto.CertificateDtos.CertificateSummaryResponse;
import com.codingplatform.entity.Certificate;
import com.codingplatform.entity.CertificateTier;
import com.codingplatform.entity.User;
import com.codingplatform.exception.ResourceNotFoundException;
import com.codingplatform.repository.CertificateRepository;
import com.codingplatform.repository.SubmissionRepository;
import com.codingplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    @Value("${app.certificates.bronze-threshold:5}")
    private int bronzeThreshold;
    @Value("${app.certificates.silver-threshold:15}")
    private int silverThreshold;
    @Value("${app.certificates.gold-threshold:30}")
    private int goldThreshold;

    private static final SecureRandom RANDOM = new SecureRandom();

    private Map<CertificateTier, Integer> thresholds() {
        Map<CertificateTier, Integer> map = new LinkedHashMap<>();
        map.put(CertificateTier.BRONZE, bronzeThreshold);
        map.put(CertificateTier.SILVER, silverThreshold);
        map.put(CertificateTier.GOLD, goldThreshold);
        return map;
    }

    /** Call after any accepted, non-run-only submission. Issues any newly-earned certificate tiers. */
    @Transactional
    public void checkAndIssue(User user) {
        long solved = submissionRepository.countDistinctSolvedProblems(user.getId());
        for (Map.Entry<CertificateTier, Integer> entry : thresholds().entrySet()) {
            CertificateTier tier = entry.getKey();
            int threshold = entry.getValue();
            if (solved >= threshold && !certificateRepository.existsByUserIdAndTier(user.getId(), tier)) {
                Certificate cert = Certificate.builder()
                        .user(user)
                        .tier(tier)
                        .problemsSolvedAtIssue((int) solved)
                        .serialCode(generateSerialCode(tier))
                        .build();
                certificateRepository.save(cert);
            }
        }
    }

    @Transactional(readOnly = true)
    public CertificateSummaryResponse getSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        long solved = submissionRepository.countDistinctSolvedProblems(userId);

        List<Certificate> owned = certificateRepository.findByUserIdOrderByIssuedAtAsc(userId);
        List<CertificateResponse> earned = new ArrayList<>();
        for (Certificate c : owned) {
            earned.add(new CertificateResponse(c.getTier(), c.getProblemsSolvedAtIssue(), c.getSerialCode(), c.getIssuedAt()));
        }

        CertificateTier nextTier = null;
        Integer remaining = null;
        for (Map.Entry<CertificateTier, Integer> entry : thresholds().entrySet()) {
            if (solved < entry.getValue()) {
                nextTier = entry.getKey();
                remaining = (int) (entry.getValue() - solved);
                break;
            }
        }

        CertificateSummaryResponse res = new CertificateSummaryResponse();
        res.setTotalSolved(solved);
        res.setEarned(earned);
        res.setNextTier(nextTier);
        res.setProblemsUntilNextTier(remaining);
        res.setUsername(user.getUsername());
        return res;
    }

    /** Publicly verifiable lookup — used by a "verify this certificate" link/page. */
    @Transactional(readOnly = true)
    public Certificate verify(String serialCode) {
        return certificateRepository.findBySerialCode(serialCode)
                .orElseThrow(() -> new ResourceNotFoundException("No certificate found with that serial code"));
    }

    private String generateSerialCode(CertificateTier tier) {
        String prefix = switch (tier) {
            case BRONZE -> "CB-BRZ";
            case SILVER -> "CB-SLV";
            case GOLD -> "CB-GLD";
        };
        StringBuilder sb = new StringBuilder(prefix).append('-');
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no ambiguous 0/O/1/I
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
