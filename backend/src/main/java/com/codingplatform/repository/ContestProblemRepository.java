package com.codingplatform.repository;

import com.codingplatform.entity.ContestProblem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContestProblemRepository extends JpaRepository<ContestProblem, Long> {
    List<ContestProblem> findByContestIdOrderByDisplayOrderAsc(Long contestId);
    Optional<ContestProblem> findByContestIdAndProblemId(Long contestId, Long problemId);
}
