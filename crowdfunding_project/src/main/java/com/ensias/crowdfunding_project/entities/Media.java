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
@ToString  // ← Seulement @ToString, sans "of"
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

    public enum TypeMedia {
        IMAGE, VIDEO
    }
}