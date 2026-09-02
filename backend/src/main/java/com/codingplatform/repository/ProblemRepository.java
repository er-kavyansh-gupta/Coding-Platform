package com.codingplatform.repository;

import com.codingplatform.entity.Difficulty;
import com.codingplatform.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    @Query("SELECT DISTINCT p FROM Problem p LEFT JOIN p.tags t WHERE " +
           "(:difficulty IS NULL OR p.difficulty = :difficulty) AND " +
           "(:tag IS NULL OR t = :tag) AND " +
           "(:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Problem> search(@Param("difficulty") Difficulty difficulty,
                          @Param("tag") String tag,
                          @Param("search") String search);
}
