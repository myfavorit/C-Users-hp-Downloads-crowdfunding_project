package com.ensias.crowdfunding_project.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "INVESTISSEMENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Investissement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement", length = 50)
    @Builder.Default
    private ModePaiement modePaiement = ModePaiement.SIMULATION;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── Enums ─────────────────────────────────────────────────────────────

    public enum StatutPaiement {
        EN_ATTENTE,
        CONFIRME,
        ECHOUE
    }

    public enum ModePaiement {
        SIMULATION,
        VIREMENT,
        CARTE
    }

    // ── Méthodes métier ─────────────────────────────────────────────────

    public boolean isMontantValide() {
        return montant != null && montant.compareTo(BigDecimal.valueOf(10)) >= 0;
    }

    public void confirmer() {
        if (this.statutPaiement != StatutPaiement.EN_ATTENTE) {
            throw new IllegalStateException(
                    "Seul un paiement en attente peut être confirmé"
            );
        }
        if (!isMontantValide()) {
            throw new IllegalStateException(
                    "Le montant minimum d'investissement est de 10"
            );
        }
        this.statutPaiement = StatutPaiement.CONFIRME;
    }

    public void echouer() {
        if (this.statutPaiement == StatutPaiement.CONFIRME) {
            throw new IllegalStateException("Un paiement confirmé ne peut pas être marqué comme échoué");
        }
        if (this.statutPaiement == StatutPaiement.ECHOUE) {
            throw new IllegalStateException("Le paiement est déjà en état d'échec");
        }
        this.statutPaiement = StatutPaiement.ECHOUE;
    }

    public boolean estConfirme() {
        return this.statutPaiement == StatutPaiement.CONFIRME;
    }

    public boolean estEnAttente() {
        return this.statutPaiement == StatutPaiement.EN_ATTENTE;
    }

    public boolean estEchoue() {
        return this.statutPaiement == StatutPaiement.ECHOUE;
    }

    public boolean estSimulation() {
        return this.modePaiement == ModePaiement.SIMULATION;
    }

    // ── toString ─────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Investissement{" +
                "id=" + id +
                ", investisseurId=" + (investisseur != null ? investisseur.getId() : null) +
                ", projetId=" + (projet != null ? projet.getId() : null) +
                ", montant=" + montant +
                ", statutPaiement=" + statutPaiement +
                ", referencePaiement='" + referencePaiement + '\'' +
                ", modePaiement=" + modePaiement +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}