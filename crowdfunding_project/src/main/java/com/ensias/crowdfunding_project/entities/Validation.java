package com.ensias.crowdfunding_project.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "VALIDATION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "decision", "decidedAt"})
public class Validation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    private Projet projet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false, columnDefinition = "BINARY(16)")
    private Utilisateur admin;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 50)
    private Decision decision;

    @Column(name = "motif_refus", columnDefinition = "TEXT")
    private String motifRefus;

    @Column(name = "decided_at", nullable = false, updatable = false)
    private LocalDateTime decidedAt;

    @PrePersist
    protected void onCreate() {
        this.decidedAt = LocalDateTime.now();
    }

    // ── Enum ─────────────────────────────────────────────────────────────

    public enum Decision {
        VALIDE,
        REJETE
    }

    // ── Méthodes métier ─────────────────────────────────────────────────

    /**
     * Vérifie si la validation est un refus
     */
    public boolean isRefus() {
        return this.decision == Decision.REJETE;
    }

    /**
     * Vérifie si la validation est une validation
     */
    public boolean isValide() {
        return this.decision == Decision.VALIDE;
    }

    /**
     * Vérifie si le motif de refus est présent (obligatoire pour REJETE)
     */
    public boolean hasMotifRefus() {
        if (this.decision == Decision.REJETE) {
            return motifRefus != null && !motifRefus.isBlank();
        }
        return true;
    }

    /**
     * Valide le projet (création d'une validation)
     */
    public static Validation valider(Projet projet, Utilisateur admin) {
        if (projet == null || admin == null) {
            throw new IllegalArgumentException("Projet et admin requis");
        }
        if (!admin.isAdmin()) {
            throw new IllegalStateException("Seul un admin peut valider un projet");
        }
        return Validation.builder()
                .projet(projet)
                .admin(admin)
                .decision(Decision.VALIDE)
                .build();
    }

    /**
     * Rejette le projet (création d'une validation avec motif)
     */
    public static Validation rejeter(Projet projet, Utilisateur admin, String motif) {
        if (projet == null || admin == null) {
            throw new IllegalArgumentException("Projet et admin requis");
        }
        if (!admin.isAdmin()) {
            throw new IllegalStateException("Seul un admin peut rejeter un projet");
        }
        if (motif == null || motif.isBlank()) {
            throw new IllegalArgumentException("Le motif de refus est obligatoire");
        }
        return Validation.builder()
                .projet(projet)
                .admin(admin)
                .decision(Decision.REJETE)
                .motifRefus(motif)
                .build();
    }
}