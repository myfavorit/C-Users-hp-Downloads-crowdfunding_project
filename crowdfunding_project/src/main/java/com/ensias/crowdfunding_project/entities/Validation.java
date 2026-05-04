package com.ensias.crowdfunding_project.entities;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "VALIDATION")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Validation {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    // 1 projet = max 1 validation (UNIQUE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    private Projet projet;

    // Admin qui a pris la décision
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false, columnDefinition = "BINARY(16)")
    private Utilisateur admin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Decision decision;

    @Column(name = "motif_refus", columnDefinition = "TEXT")
    private String motifRefus;

    @Column(name = "decided_at", nullable = false, updatable = false)
    private LocalDateTime decidedAt;

    @PrePersist
    protected void onCreate() {
        this.decidedAt = LocalDateTime.now();
    }

    public enum Decision {
        VALIDE,
        REJETE
    }
}