package com.codingplatform.controller;

import com.codingplatform.dto.SubmissionDtos.SubmissionHistoryItem;
import com.codingplatform.dto.SubmissionDtos.SubmissionRequest;
import com.codingplatform.dto.SubmissionDtos.SubmissionResponse;
import com.codingplatform.security.UserPrincipal;
import com.codingplatform.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<SubmissionResponse> submit(
            @Valid @RequestBody SubmissionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(submissionService.submit(principal.getId(), request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionResponse> get(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = "ADMIN".equals(principal.getRole());
        return ResponseEntity.ok(submissionService.getSubmission(id, principal.getId(), isAdmin));
    }

    @GetMapping("/history")
    public ResponseEntity<List<SubmissionHistoryItem>> history(
            @RequestParam(required = false) Long problemId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(submissionService.history(principal.getId(), problemId));
    }
}
