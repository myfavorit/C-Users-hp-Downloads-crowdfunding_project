package com.ensias.crowdfunding_project.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "COMMENTAIRE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Commentaires {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false, columnDefinition = "BINARY(16)")
    private Projet projet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auteur_id", nullable = false, columnDefinition = "BINARY(16)")
    @ToString.Exclude
    private Utilisateur auteur;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Méthodes métier ─────────────────────────────────────────────────

    /**
     * Vérifie si le contenu est valide (non vide)
     */
    public boolean isContenuValide() {
        return contenu != null && !contenu.isBlank() && contenu.length() <= 2000;
    }

    /**
     * Édite le contenu du commentaire
     */
    public void editer(String nouveauContenu) {
        if (nouveauContenu == null || nouveauContenu.isBlank()) {
            throw new IllegalArgumentException("Le contenu ne peut pas être vide");
        }
        if (nouveauContenu.length() > 2000) {
            throw new IllegalArgumentException("Le contenu ne peut pas dépasser 2000 caractères");
        }
        this.contenu = nouveauContenu;
    }

    /**
     * Vérifie si l'utilisateur est l'auteur du commentaire
     */
    public boolean estAuteur(UUID utilisateurId) {
        return this.auteur.getId().equals(utilisateurId);
    }
}