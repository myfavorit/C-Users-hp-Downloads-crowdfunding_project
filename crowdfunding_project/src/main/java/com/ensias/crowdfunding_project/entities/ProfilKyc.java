package com.ensias.crowdfunding_project.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "PROFIL_KYC")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfilKyc {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    // ── Relation 1-1 avec Utilisateur ────────────────────────────────────
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false, unique = true,
            columnDefinition = "BINARY(16)")
    private Utilisateur utilisateur;

    // ── Champs KYC ──────────────────────────────────────────────────────
    @Column(name = "photo_profil", length = 255)
    private String photoProfil;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false, length = 255)
    private String rib;

    // ── État de validation ──────────────────────────────────────────────
    @Column(name = "kyc_valide", nullable = false)
    @Builder.Default
    private boolean kycValide = false;

    @Column(name = "kyc_soumis_at")
    private LocalDateTime kycSoumisAt;

    @Column(name = "kyc_valide_at")
    private LocalDateTime kycValideAt;

    // ── Lifecycle ────────────────────────────────────────────────────────
    @PrePersist
    protected void onCreate() {
        if (this.kycSoumisAt == null) {
            this.kycSoumisAt = LocalDateTime.now();
        }
    }

    // ── Helpers métier ───────────────────────────────────────────────────

    /**
     * Valide le KYC
     */
    public void valider() {
        if (this.kycValide) {
            throw new IllegalStateException("Le KYC est déjà validé");
        }
        this.kycValide = true;
        this.kycValideAt = LocalDateTime.now();
    }

    /**
     * Rejette le KYC - l'utilisateur devra resoumettre
     */
    public void rejeter() {
        this.kycValide = false;
        this.kycValideAt = null;
        this.kycSoumisAt = null;
    }

    /**
     * Vérifie si le KYC est validé
     */
    public boolean estValide() {
        return this.kycValide;
    }

    /**
     * Vérifie si le KYC est en attente de validation
     */
    public boolean estEnAttente() {
        return !this.kycValide && this.kycSoumisAt != null;
    }

    /**
     * Vérifie si l'utilisateur a déjà soumis un KYC
     */
    public boolean aEteSoumis() {
        return this.kycSoumisAt != null;
    }

    @Override
    public String toString() {
        return "ProfilKyc{" +
                "id=" + id +
                ", utilisateurId=" + (utilisateur != null ? utilisateur.getId() : null) +
                ", kycValide=" + kycValide +
                ", kycSoumisAt=" + kycSoumisAt +
                '}';
    }
}