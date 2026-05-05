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
@ToString  // ← Seulement @ToString, sans "of"
public class Commentaires {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false, columnDefinition = "BINARY(16)")
    @ToString.Exclude
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
}