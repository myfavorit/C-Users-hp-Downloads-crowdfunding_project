package com.ensias.crowdfunding_project.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "NOTIFICATION")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_id", nullable = false, columnDefinition = "BINARY(16)")
    private Utilisateur destinataire;

    @Column(nullable = false, length = 255)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TypeNotification type;

    @Column(nullable = false)
    @Builder.Default
    private boolean lu = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum TypeNotification {
        PROJET_VALIDE,
        PROJET_REJETE,
        INVESTISSEMENT_RECU,
        KYC_VALIDE,
        KYC_REJETE,
        REMBOURSEMENT,
        SYSTEME
    }
}

