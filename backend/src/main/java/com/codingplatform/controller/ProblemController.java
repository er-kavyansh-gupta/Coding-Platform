package com.codingplatform.controller;

import com.codingplatform.dto.ProblemDtos.ProblemDetailResponse;
import com.codingplatform.dto.ProblemDtos.ProblemSummaryResponse;
import com.codingplatform.entity.Difficulty;
import com.codingplatform.security.UserPrincipal;
import com.codingplatform.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping
    public ResponseEntity<List<ProblemSummaryResponse>> list(
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(problemService.list(difficulty, tag, search, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProblemDetailResponse> getDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(problemService.getDetail(id, userId));
    }
}
