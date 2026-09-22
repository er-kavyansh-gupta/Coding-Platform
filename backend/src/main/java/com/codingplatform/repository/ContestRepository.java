package com.codingplatform.repository;

import com.codingplatform.entity.Contest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContestRepository extends JpaRepository<Contest, Long> {
    List<Contest> findByIsPublishedTrueOrderByStartTimeDesc();
    List<Contest> findAllByOrderByStartTimeDesc();
}
