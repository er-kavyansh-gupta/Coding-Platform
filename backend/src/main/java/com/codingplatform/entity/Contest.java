package com.codingplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "organization_name", nullable = false, length = 150)
    private String organizationName;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Markdown: rules, eligibility, instructions, etc.

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /** Draft contests are only visible/editable to admins; published ones are visible to all users. */
    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private Boolean isPublished = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "contest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContestProblem> contestProblems = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @Transient
    public ContestStatus computeStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) return ContestStatus.UPCOMING;
        if (now.isAfter(endTime)) return ContestStatus.ENDED;
        return ContestStatus.ONGOING;
    }
}
