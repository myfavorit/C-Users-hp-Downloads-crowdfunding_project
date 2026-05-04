package com.ensias.crowdfunding_project.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

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
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Favori {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false, columnDefinition = "BINARY(16)")
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false, columnDefinition = "BINARY(16)")
    private Projet projet;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

