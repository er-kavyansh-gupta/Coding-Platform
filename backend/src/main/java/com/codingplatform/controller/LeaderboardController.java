package com.codingplatform.controller;

import com.codingplatform.dto.StatsDtos.LeaderboardEntryResponse;
import com.codingplatform.entity.Language;
import com.codingplatform.service.LeaderboardService;
import com.codingplatform.service.LeaderboardService.Scope;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    public ResponseEntity<List<LeaderboardEntryResponse>> getLeaderboard(
            @RequestParam(defaultValue = "GLOBAL") Scope scope,
            @RequestParam(required = false) Language language,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(leaderboardService.getLeaderboard(scope, language, limit));
    }
}
