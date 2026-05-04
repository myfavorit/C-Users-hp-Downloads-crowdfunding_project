package com.ensias.crowdfunding_project.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "UTILISATEUR")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Utilisateur {

    // ── ID binary(16) stocké comme UUID ──────────────────────────────────
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String nom;

    @Column(nullable = false, length = 255)
    private String prenom;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "mot_de_passe_hash", length = 255)
    private String motDePasseHash;

    // ── Rôle : USER (par défaut) ou ADMIN ────────────────────────────────
    // Valeurs possibles : 'USER', 'ADMIN'
    // USER = utilisateur normal (porteur/investisseur selon ses actions)
    // ADMIN = droits de validation projets + KYC
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private Role role = Role.USER;

    // ── Statut du compte ─────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private StatutCompte statut = StatutCompte.ACTIF;

    // ── OTP (One Time Password) pour vérification email ──────────────────
    @Column(name = "otp_code", length = 255)
    private String otpCode;

    @Column(name = "otp_expiration")
    private LocalDateTime otpExpiration;

    // ── OAuth2 (Google, Facebook…) ────────────────────────────────────────
    @Column(name = "oauth_provider", length = 255)
    private String oauthProvider;

    @Column(name = "oauth_id", length = 255)
    private String oauthId;

    // ── Soft delete ───────────────────────────────────────────────────────
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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
    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ProfilKyc profilKyc;

    @OneToMany(mappedBy = "porteur", fetch = FetchType.LAZY)
    private List<Projet> projets;

    @OneToMany(mappedBy = "investisseur", fetch = FetchType.LAZY)
    private List<Investissement> investissements;

    @OneToMany(mappedBy = "destinataire", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notification> notifications;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Favori> favoris;

    @OneToMany(mappedBy = "auteur", fetch = FetchType.LAZY)
    private List<Commentaires> commentaires;

    // ── Enums internes ────────────────────────────────────────────────────
    public enum Role {
        USER,   // porteur et/ou investisseur selon ses actions
        ADMIN   // peut valider projets et KYC
    }

    public enum StatutCompte {
        ACTIF,
        SUSPENDU
    }

    // ── Helpers métier ────────────────────────────────────────────────────
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public boolean hasKycValide() {
        return this.profilKyc != null && this.profilKyc.isKycValide();
    }

    public boolean peutInvestir() {
        return !this.isDeleted
                && this.statut == StatutCompte.ACTIF
                && this.hasKycValide();
    }

    public boolean peutCreerProjet() {
        return peutInvestir(); // même condition
    }
}