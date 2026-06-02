package com.ensias.crowdfunding_project.entities;

import com.ensias.crowdfunding_project.enums.DomaineProjet;
import com.ensias.crowdfunding_project.enums.StatutProjet;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "PROJET")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "titre", "domaine", "statut", "montantActuel"})
public class Projet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "porteur_id", nullable = false, columnDefinition = "BINARY(16)")
    private Utilisateur porteur;

    @Column(name = "titre", nullable = false, length = 255)
    private String titre;

    @Column(name = "description_prjt", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_principale", length = 255)
    private String imagePrincipale;

    @Enumerated(EnumType.STRING)
    @Column(name = "domaine", nullable = false, length = 100)
    private DomaineProjet domaine;

    @Column(name = "objectif_financier", nullable = false, precision = 15, scale = 2)
    private BigDecimal objectifFinancier;

    @Setter(AccessLevel.NONE)
    @Column(name = "montant_actuel", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal montantActuel = BigDecimal.ZERO;

    @Column(name = "duree_jours", nullable = false)
    private Integer dureeJours;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "pourcentage_offert", precision = 5, scale = 2)
    private BigDecimal pourcentageOffert;

    @Column(name = "justification_valuation", columnDefinition = "TEXT")
    private String justificationValuation;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 50)
    @Builder.Default
    private StatutProjet statut = StatutProjet.EN_ATTENTE;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── Relations ─────────────────────────────────────────────────────────
    @OneToOne(mappedBy = "projet", fetch = FetchType.LAZY)
    private Validation validation;

    @OneToMany(mappedBy = "projet", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Investissement> investissements = new ArrayList<>();

    @OneToMany(mappedBy = "projet", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Media> medias = new ArrayList<>();

    @OneToMany(mappedBy = "projet", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Favori> favoris = new ArrayList<>();

    @OneToMany(mappedBy = "projet", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Commentaires> commentaires = new ArrayList<>();

    // ── Vérification ──────────────────────────────────────────────────────
    public boolean isComplet() {
        return titre != null && !titre.isBlank()
                && description != null && !description.isBlank()
                && domaine != null
                && objectifFinancier != null && objectifFinancier.compareTo(BigDecimal.ZERO) > 0
                && dureeJours != null && dureeJours >= 7 && dureeJours <= 90
                && (pourcentageOffert == null || (pourcentageOffert.compareTo(BigDecimal.valueOf(1)) >= 0
                && pourcentageOffert.compareTo(BigDecimal.valueOf(49)) <= 0));
    }

    // ── Méthodes métier ───────────────────────────────────────────────────
    public void soumettre() {
        if (this.statut != StatutProjet.BROUILLON) {
            throw new IllegalStateException("Seul un projet en brouillon peut être soumis");
        }
        if (!isComplet()) {
            throw new IllegalStateException("Le projet n'est pas complet");
        }
        this.statut = StatutProjet.EN_ATTENTE;
    }

    public void valider() {
        if (this.statut != StatutProjet.EN_ATTENTE) {
            throw new IllegalStateException("Seul un projet en attente peut être validé");
        }
        this.statut = StatutProjet.VALIDE;
        this.dateDebut = LocalDate.now();
        this.dateFin = this.dateDebut.plusDays(this.dureeJours);
    }

    public void refuser() {
        if (this.statut != StatutProjet.EN_ATTENTE) {
            throw new IllegalStateException("Seul un projet en attente peut être refusé");
        }
        this.statut = StatutProjet.REJETE;
    }

    public void annuler() {
        if (this.statut == StatutProjet.CLOTURE_SUCCES || this.statut == StatutProjet.ECHEC_REMBOURSE) {
            throw new IllegalStateException("Un projet clôturé ne peut pas être annulé");
        }
        if (this.montantActuel.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Impossible d'annuler : des investissements existent");
        }
        this.statut = StatutProjet.ANNULE;
    }

    public void ajouterInvestissement(BigDecimal montant) {
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }
        if (this.statut != StatutProjet.VALIDE) {
            throw new IllegalStateException("Le projet n'est pas ouvert aux investissements");
        }
        if (this.dateFin != null && LocalDate.now().isAfter(this.dateFin)) {
            throw new IllegalStateException("La campagne est terminée");
        }

        this.montantActuel = this.montantActuel.add(montant);

        if (this.montantActuel.compareTo(this.objectifFinancier) >= 0) {
            this.statut = StatutProjet.CLOTURE_SUCCES;
        }
    }

    public void cloturer() {
        if (this.statut != StatutProjet.VALIDE) {
            throw new IllegalStateException("Seul un projet validé peut être clôturé");
        }
        if (isObjectifAtteint()) {
            this.statut = StatutProjet.CLOTURE_SUCCES;
        } else {
            this.statut = StatutProjet.ECHEC_REMBOURSE;
        }
    }

    public boolean estOuvert() {
        return this.statut == StatutProjet.VALIDE
                && !Boolean.TRUE.equals(this.isDeleted)
                && this.dateFin != null
                && !LocalDate.now().isAfter(this.dateFin);
    }

    public boolean isEchec() {
        return this.statut == StatutProjet.ECHEC_REMBOURSE;
    }

    public boolean isSucces() {
        return this.statut == StatutProjet.CLOTURE_SUCCES;
    }

    public BigDecimal getPourcentageFinancement() {
        if (objectifFinancier == null || objectifFinancier.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return montantActuel
                .multiply(BigDecimal.valueOf(100))
                .divide(objectifFinancier, 2, java.math.RoundingMode.HALF_UP);
    }

    // ========== MÉTHODES UTILITAIRES ==========
    public BigDecimal getMontantRestant() {
        if (objectifFinancier == null) return BigDecimal.ZERO;
        BigDecimal restant = objectifFinancier.subtract(montantActuel);
        return restant.compareTo(BigDecimal.ZERO) > 0 ? restant : BigDecimal.ZERO;
    }

    public boolean isObjectifAtteint() {
        return montantActuel != null && objectifFinancier != null &&
                montantActuel.compareTo(objectifFinancier) >= 0;
    }

    public boolean peutEtreSupprimePhysiquement() {
        return this.montantActuel.compareTo(BigDecimal.ZERO) == 0;
    }

    public void softDelete() {
        this.isDeleted = true;
    }

    @Override
    public String toString() {
        return "Projet{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", domaine=" + domaine +
                ", statut=" + statut +
                ", montantActuel=" + montantActuel +
                ", objectifFinancier=" + objectifFinancier +
                '}';
    }
}