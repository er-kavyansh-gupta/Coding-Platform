package com.codingplatform.controller;

import com.codingplatform.dto.ContestDtos.*;
import com.codingplatform.entity.Contest;
import com.codingplatform.security.UserPrincipal;
import com.codingplatform.service.ContestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contests")
@RequiredArgsConstructor
public class ContestController {

    private final ContestService contestService;

    @GetMapping
    public ResponseEntity<List<ContestSummaryResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        boolean isAdmin = principal != null && "ADMIN".equals(principal.getRole());
        return ResponseEntity.ok(contestService.list(userId, isAdmin));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContestDetailResponse> getDetail(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        boolean isAdmin = principal != null && "ADMIN".equals(principal.getRole());
        return ResponseEntity.ok(contestService.getDetail(id, userId, isAdmin));
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<Void> register(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        contestService.register(id, principal.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/leaderboard")
    public ResponseEntity<List<ContestLeaderboardEntryResponse>> leaderboard(@PathVariable Long id) {
        return ResponseEntity.ok(contestService.getLeaderboard(id));
    }

    // -----------------------------------------------------------------
    // Admin
    // -----------------------------------------------------------------

    @PostMapping("/admin")
    public ResponseEntity<Contest> create(@Valid @RequestBody ContestRequest request,
                                           @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(contestService.create(principal.getId(), request));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<Contest> update(@PathVariable Long id, @Valid @RequestBody ContestRequest request) {
        return ResponseEntity.ok(contestService.update(id, request));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contestService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
