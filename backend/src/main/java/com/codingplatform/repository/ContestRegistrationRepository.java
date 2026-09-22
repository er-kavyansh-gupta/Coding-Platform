package com.codingplatform.repository;

import com.codingplatform.entity.ContestRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContestRegistrationRepository extends JpaRepository<ContestRegistration, Long> {
    boolean existsByContestIdAndUserId(Long contestId, Long userId);
    Optional<ContestRegistration> findByContestIdAndUserId(Long contestId, Long userId);
    List<ContestRegistration> findByContestId(Long contestId);
    long countByContestId(Long contestId);
}
