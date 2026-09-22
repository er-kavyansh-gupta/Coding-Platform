package com.codingplatform.repository;

import com.codingplatform.entity.Certificate;
import com.codingplatform.entity.CertificateTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByUserIdOrderByIssuedAtAsc(Long userId);
    boolean existsByUserIdAndTier(Long userId, CertificateTier tier);
    Optional<Certificate> findBySerialCode(String serialCode);
}
