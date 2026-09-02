package com.codingplatform.controller;

import com.codingplatform.dto.ProblemDtos.ProblemRequest;
import com.codingplatform.entity.Problem;
import com.codingplatform.repository.SubmissionRepository;
import com.codingplatform.repository.UserRepository;
import com.codingplatform.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProblemService problemService;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;

    @PostMapping("/problems")
    public ResponseEntity<Problem> createProblem(@Valid @RequestBody ProblemRequest request) {
        return ResponseEntity.ok(problemService.create(request));
    }

    @PutMapping("/problems/{id}")
    public ResponseEntity<Problem> updateProblem(@PathVariable Long id, @Valid @RequestBody ProblemRequest request) {
        return ResponseEntity.ok(problemService.update(id, request));
    }

    @DeleteMapping("/problems/{id}")
    public ResponseEntity<Void> deleteProblem(@PathVariable Long id) {
        problemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Basic platform usage analytics for the admin panel. */
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> analytics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalSubmissions", submissionRepository.count());
        return ResponseEntity.ok(stats);
    }
}
