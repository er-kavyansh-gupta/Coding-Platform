package com.codingplatform.repository;

import com.codingplatform.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    List<TestCase> findByProblemIdOrderByDisplayOrderAsc(Long problemId);
    List<TestCase> findByProblemIdAndIsHiddenFalseOrderByDisplayOrderAsc(Long problemId);
}
