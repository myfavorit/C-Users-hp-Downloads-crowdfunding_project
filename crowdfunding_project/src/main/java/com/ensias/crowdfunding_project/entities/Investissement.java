package com.ensias.crowdfunding_project.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "INVESTISSEMENT")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Investissement {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    // L'utilisateur devient investisseur implicitement ici
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investisseur_id", nullable = false, columnDefinition = "BINARY(16)")
    private Utilisateur investisseur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false, columnDefinition = "BINARY(16)")
    private Projet projet;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_paiement", nullable = false, length = 50)
    @Builder.Default
    private StatutPaiement statutPaiement = StatutPaiement.EN_ATTENTE;

    @Column(name = "reference_paiement", unique = true, length = 255)
    private String referencePaiement;

    @Column(name = "mode_paiement", length = 255)
    private String modePaiement;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum StatutPaiement {
        EN_ATTENTE,
        VALIDE,
        ECHOUE,
        REMBOURSE,
        ANNULE
    }
}
