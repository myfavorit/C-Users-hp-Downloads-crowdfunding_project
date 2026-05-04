package com.ensias.crowdfunding_project.entities;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "PROJET")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Projet {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    // ── Porteur du projet ─────────────────────────────────────────────────
    // L'utilisateur devient porteur implicitement par la création de ce projet
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "porteur_id", nullable = false, columnDefinition = "BINARY(16)")
    private Utilisateur porteur;

    @Column(nullable = false, length = 255)
    private String titre;

    @Column(name = "description_prjt", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_principale", length = 255)
    private String imagePrincipale;

    // ── Domaine (remplace CATEGORIE table — simplifié dans ta version finale)
    @Column(nullable = false, length = 100)
    private String domaine;

    // ── Financement ───────────────────────────────────────────────────────
    @Column(name = "objectif_financier", nullable = false, precision = 15, scale = 2)
    private BigDecimal objectifFinancier;

    // Maintenu par trigger SQL — ne jamais modifier directement depuis le code
    @Column(name = "montant_actuel", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal montantActuel = BigDecimal.ZERO;

    @Column(name = "duree_jours", nullable = false)
    private Integer dureeJours;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    // ── Equity crowdfunding ───────────────────────────────────────────────
    // % du capital offert aux investisseurs
    @Column(name = "pourcentage_offert", precision = 5, scale = 2)
    private BigDecimal pourcentageOffert;

    // Justification de la valorisation de l'entreprise
    @Column(name = "justification_valuation", columnDefinition = "TEXT")
    private String justificationValuation;

    // ── Statut du projet ──────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private StatutProjet statut = StatutProjet.BROUILLON;

    // ── Soft delete ───────────────────────────────────────────────────────
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    // ── Audit ─────────────────────────────────────────────────────────────
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

    // ── Relations ─────────────────────────────────────────────────────────
    @OneToOne(mappedBy = "projet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Validation validation;

    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Investissement> investissements;

    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Media> medias;

    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Favori> favoris;

    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Commentaires> commentaires;

    // ── Enum Statut ───────────────────────────────────────────────────────
    public enum StatutProjet {
        BROUILLON,    // créé, pas encore soumis
        EN_ATTENTE,   // soumis, en attente de validation admin
        VALIDE,       // approuvé — visible et finançable
        REJETE,       // refusé par l'admin
        CLOTURE       // campagne terminée (succès ou échec)
    }

    // ── Helpers métier ────────────────────────────────────────────────────
    public boolean estFinance() {
        return montantActuel.compareTo(objectifFinancier) >= 0;
    }

    public BigDecimal pourcentageFinancement() {
        if (objectifFinancier.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return montantActuel
                .multiply(BigDecimal.valueOf(100))
                .divide(objectifFinancier, 2, java.math.RoundingMode.HALF_UP);
    }

    public boolean estOuvert() {
        return this.statut == StatutProjet.VALIDE
                && !this.isDeleted
                && (dateFin == null || !LocalDate.now().isAfter(dateFin));
    }
}