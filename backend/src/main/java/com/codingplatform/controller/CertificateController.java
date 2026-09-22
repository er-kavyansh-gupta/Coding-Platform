package com.codingplatform.controller;

import com.codingplatform.dto.CertificateDtos.CertificateSummaryResponse;
import com.codingplatform.entity.Certificate;
import com.codingplatform.security.UserPrincipal;
import com.codingplatform.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @GetMapping
    public ResponseEntity<CertificateSummaryResponse> getMySummary(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(certificateService.getSummary(principal.getId()));
    }

    /** Public endpoint (no auth) so anyone with a certificate's serial code can confirm it's genuine. */
    @GetMapping("/verify/{serialCode}")
    public ResponseEntity<Map<String, Object>> verify(@PathVariable String serialCode) {
        Certificate cert = certificateService.verify(serialCode);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("valid", true);
        body.put("username", cert.getUser().getUsername());
        body.put("tier", cert.getTier());
        body.put("problemsSolvedAtIssue", cert.getProblemsSolvedAtIssue());
        body.put("issuedAt", cert.getIssuedAt());
        return ResponseEntity.ok(body);
    }
}
