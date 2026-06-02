package com.ensias.crowdfunding_project.entities;

import com.ensias.crowdfunding_project.enums.RoleUtilisateur;
import com.ensias.crowdfunding_project.enums.StatutCompte;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "UTILISATEUR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private RoleUtilisateur role = RoleUtilisateur.INVESTOR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private StatutCompte statut = StatutCompte.INACTIF;

    // --- OTP & OAuth ---
    @Column(name = "otp_code", length = 255)
    private String otpCode;

    @Column(name = "otp_expiration")
    private LocalDateTime otpExpiration;

    @Column(name = "oauth_provider", length = 255)
    private String oauthProvider;

    @Column(name = "oauth_id", length = 255)
    private String oauthId;

    @Column(name = "otp_generation_count", nullable = false)
    @Builder.Default
    private int otpGenerationCount = 0;

    // --- Audit ---
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "otp_attempts", nullable = false)
    @Builder.Default
    private int otpAttempts = 0;

    /** Nombre de tentatives de connexion échouées consécutives */
    @Column(name = "login_attempts", nullable = false)
    private int loginAttempts = 0;

    /** Date/heure jusqu'à laquelle le compte est verrouillé (null = non verrouillé) */
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    // --- Reset password ---
    @Column(name = "reset_token", length = 255)
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Relations ---
    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ProfilKyc profilKyc;

    @OneToMany(mappedBy = "porteur", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Projet> projets = new ArrayList<>();

    @OneToMany(mappedBy = "investisseur", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Investissement> investissements = new ArrayList<>();

    @OneToMany(mappedBy = "destinataire", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Favori> favoris = new ArrayList<>();

    @OneToMany(mappedBy = "auteur", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Commentaires> commentaires = new ArrayList<>();

    // --- Enum interne (StatutCompte) ---
    //public enum StatutCompte {
       // ACTIF, INACTIF, SUSPENDU, BANNI
    //}

    // --- Helpers Métier ---
    public boolean hasKycValide() {
        return profilKyc != null && profilKyc.isKycValide();
    }

    public boolean isAdmin() {
        return this.role == RoleUtilisateur.ADMIN;
    }

    public boolean isProjectCreator() {
        return this.role == RoleUtilisateur.PROJECT_CREATOR;
    }

    public boolean isInvestor() {
        return this.role == RoleUtilisateur.INVESTOR;
    }

    public boolean peutInvestir() {
        return this.statut == StatutCompte.ACTIF
                && hasKycValide()
                && (this.role == RoleUtilisateur.INVESTOR || this.role == RoleUtilisateur.PROJECT_CREATOR);
    }

    public boolean peutCreerProjet() {
        return this.statut == StatutCompte.ACTIF
                && hasKycValide()
                && this.role == RoleUtilisateur.PROJECT_CREATOR;
    }

    public boolean peutValiderKyc() {
        return this.statut == StatutCompte.ACTIF && this.role == RoleUtilisateur.ADMIN;
    }

    public boolean isKycEnAttente() {
        return profilKyc != null && !profilKyc.isKycValide();
    }

    public boolean aSoumisKyc() {
        return profilKyc != null;
    }

    public boolean isActif() {
        return this.statut == StatutCompte.ACTIF;
    }

    public boolean isOAuthUser() {
        return this.motDePasseHash == null && this.oauthId != null;
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", statut=" + statut +
                '}';
    }
    // Ajouter dans l'entité Utilisateur.java
}