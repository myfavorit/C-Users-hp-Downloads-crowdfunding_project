package com.ensias.crowdfunding_project.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "FAVORI",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_favori_user_projet",
                columnNames = {"utilisateur_id", "projet_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "createdAt"})
public class Favori {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false, columnDefinition = "BINARY(16)")
    @ToString.Exclude
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false, columnDefinition = "BINARY(16)")
    @ToString.Exclude
    private Projet projet;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Méthodes métier ─────────────────────────────────────────────────

    /**
     * Vérifie si le favori est valide (utilisateur et projet non null)
     */
    public boolean estValide() {
        return utilisateur != null && projet != null;
    }

    /**
     * Vérifie si l'utilisateur est propriétaire du favori
     */
    public boolean estProprietaire(UUID utilisateurId) {
        return this.utilisateur.getId().equals(utilisateurId);
    }
}