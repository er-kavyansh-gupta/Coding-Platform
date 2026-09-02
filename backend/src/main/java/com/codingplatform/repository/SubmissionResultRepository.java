package com.codingplatform.repository;

import com.codingplatform.entity.SubmissionResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionResultRepository extends JpaRepository<SubmissionResult, Long> {
    List<SubmissionResult> findBySubmissionIdOrderByIdAsc(Long submissionId);
}
