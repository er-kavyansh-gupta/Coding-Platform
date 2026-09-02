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
@Table(name = "problems")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Markdown

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;

    @ElementCollection
    @CollectionTable(name = "problem_tags", joinColumns = @JoinColumn(name = "problem_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Lob
    @Column(name = "hints", columnDefinition = "TEXT")
    private String hintsJson; // JSON array of step-by-step hints

    @Lob
    @Column(name = "editorial", columnDefinition = "TEXT")
    private String editorial; // full editorial solution (markdown)

    @Column(name = "unlock_editorial_after_failures")
    @Builder.Default
    private Integer unlockEditorialAfterFailures = 3;

    @Lob
    @Column(name = "starter_code_java", columnDefinition = "TEXT")
    private String starterCodeJava;

    @Lob
    @Column(name = "starter_code_python", columnDefinition = "TEXT")
    private String starterCodePython;

    @Lob
    @Column(name = "starter_code_cpp", columnDefinition = "TEXT")
    private String starterCodeCpp;

    @Lob
    @Column(name = "starter_code_javascript", columnDefinition = "TEXT")
    private String starterCodeJavascript;

    @Column(name = "time_limit_ms")
    @Builder.Default
    private Integer timeLimitMs = 2000;

    @Column(name = "memory_limit_mb")
    @Builder.Default
    private Integer memoryLimitMb = 256;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TestCase> testCases = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
