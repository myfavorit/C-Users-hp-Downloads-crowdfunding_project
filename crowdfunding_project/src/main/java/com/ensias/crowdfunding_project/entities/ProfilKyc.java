package com.ensias.crowdfunding_project.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "PROFIL_KYC")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProfilKyc {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    // ── Relation 1-1 avec Utilisateur ────────────────────────────────────
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false, unique = true,
            columnDefinition = "BINARY(16)")
    private Utilisateur utilisateur;

    // ── Champs KYC — tous NOT NULL car soumis en une seule fois ──────────
    // Le profil n'est créé en base QUE quand l'utilisateur soumet
    // le formulaire complet (déclenché par action créer/investir)
    @Column(name = "photo_profil", nullable = false, length = 255)
    private String photoProfil;

    @Column(nullable = false, length = 255)
    private String cin;

    @Column(nullable = false, length = 255)
    private String rib;

    // ── Bio optionnelle ───────────────────────────────────────────────────
    @Column(columnDefinition = "TEXT")
    private String bio;

    // ── État de validation ────────────────────────────────────────────────
    // false  = soumis, en attente de validation admin
    // true   = validé par admin → utilisateur peut agir
    @Column(name = "kyc_valide", nullable = false)
    @Builder.Default
    private boolean kycValide = false;

    // Rempli automatiquement à l'insertion (soumission du formulaire)
    @Column(name = "kyc_soumis_at", nullable = false, updatable = false)
    private LocalDateTime kycSoumisAt;

    // Rempli par l'admin au moment de la validation
    @Column(name = "kyc_valide_at")
    private LocalDateTime kycValideAt;

    // Admin qui a validé le KYC
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valide_par", columnDefinition = "BINARY(16)")
    private Utilisateur validePar;

    @PrePersist
    protected void onCreate() {
        this.kycSoumisAt = LocalDateTime.now();
    }

    // ── Helpers métier ────────────────────────────────────────────────────

    // Appelé par l'admin pour valider le KYC
    public void valider(Utilisateur admin) {
        this.kycValide   = true;
        this.kycValideAt = LocalDateTime.now();
        this.validePar   = admin;
    }

    // Appelé par l'admin pour rejeter — remet le profil en "à corriger"
    public void rejeter() {
        this.kycValide   = false;
        this.kycValideAt = null;
        this.validePar   = null;
        this.kycSoumisAt = null; // l'utilisateur devra resoumettre
    }
}