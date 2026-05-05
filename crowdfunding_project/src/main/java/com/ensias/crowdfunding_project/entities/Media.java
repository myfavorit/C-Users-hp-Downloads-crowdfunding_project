package com.ensias.crowdfunding_project.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "MEDIA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "url", "typeMedia", "createdAt"})
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false, columnDefinition = "BINARY(16)")
    @ToString.Exclude
    private Projet projet;

    @Column(nullable = false, length = 255)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_media", nullable = false, length = 50)
    private TypeMedia typeMedia;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Enum ─────────────────────────────────────────────────────────────

    public enum TypeMedia {
        IMAGE,
        VIDEO
        // DOCUMENT supprimé car pas dans la contrainte SQL
    }

    // ── Méthodes métier ─────────────────────────────────────────────────

    /**
     * Vérifie si l'URL est valide
     */
    public boolean isUrlValide() {
        return url != null && !url.isBlank() && (url.startsWith("http://") || url.startsWith("https://"));
    }

    /**
     * Vérifie si le type de média est valide pour l'URL
     * Exemple: une URL .mp4 ne peut pas être IMAGE
     */
    public boolean isTypeCoherent() {
        if (typeMedia == TypeMedia.IMAGE) {
            return url.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|webp)$");
        } else if (typeMedia == TypeMedia.VIDEO) {
            return url.toLowerCase().matches(".*\\.(mp4|webm|avi|mov)$");
        }
        return true;
    }
}