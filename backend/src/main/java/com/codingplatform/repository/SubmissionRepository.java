package com.codingplatform.repository;

import com.codingplatform.entity.Submission;
import com.codingplatform.entity.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByUserIdOrderBySubmittedAtDesc(Long userId);

    List<Submission> findByUserIdAndProblemIdOrderBySubmittedAtDesc(Long userId, Long problemId);

    long countByUserIdAndSubmittedAtAfter(Long userId, LocalDateTime after);

    long countByUserIdAndProblemIdAndSubmittedAtAfter(Long userId, Long problemId, LocalDateTime after);

    @Query("SELECT COUNT(DISTINCT s.problem.id) FROM Submission s WHERE s.user.id = :userId AND s.status = 'ACCEPTED' AND s.isRunOnly = false")
    long countDistinctSolvedProblems(@Param("userId") Long userId);

    @Query("SELECT s FROM Submission s WHERE s.user.id = :userId AND s.problem.id = :problemId " +
           "AND s.status = 'ACCEPTED' AND s.isRunOnly = false")
    List<Submission> findAcceptedSubmissions(@Param("userId") Long userId, @Param("problemId") Long problemId);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.user.id = :userId AND s.isRunOnly = false")
    long countTotalSubmissions(@Param("userId") Long userId);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.user.id = :userId AND s.status = 'ACCEPTED' AND s.isRunOnly = false")
    long countAcceptedSubmissions(@Param("userId") Long userId);

    @Query("SELECT s.language, COUNT(s) FROM Submission s WHERE s.user.id = :userId AND s.isRunOnly = false GROUP BY s.language")
    List<Object[]> countByLanguageForUser(@Param("userId") Long userId);

    @Query("SELECT s FROM Submission s WHERE s.user.id = :userId AND s.isRunOnly = false AND s.submittedAt >= :since ORDER BY s.submittedAt ASC")
    List<Submission> findHeatmapSubmissions(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    long countByProblemIdAndIsRunOnlyFalse(Long problemId);

    long countByProblemIdAndStatusAndIsRunOnlyFalse(Long problemId, SubmissionStatus status);

    boolean existsByUserIdAndProblemIdAndStatusAndIsRunOnlyFalse(Long userId, Long problemId, SubmissionStatus status);

    long countByUserIdAndProblemIdAndIsRunOnlyFalseAndStatusNot(Long userId, Long problemId, SubmissionStatus status);
}
