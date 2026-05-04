package com.ensias.crowdfunding_project.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "UTILISATEUR")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
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
    // ⚠️ Retirez @Builder.Default si le rôle est choisi à l'inscription
    private Role role;  // Plus de valeur par défaut

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private StatutCompte statut = StatutCompte.ACTIF;

    // --- OTP & OAuth ---
    @Column(name = "otp_code", length = 255)
    private String otpCode;

    @Column(name = "otp_expiration")
    private LocalDateTime otpExpiration;

    @Column(name = "oauth_provider", length = 255)
    private String oauthProvider;

    @Column(name = "oauth_id", length = 255)
    private String oauthId;

    // --- Audit ---
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

    // --- Relations avec orphanRemoval ---
    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ProfilKyc profilKyc;

    @OneToMany(mappedBy = "porteur", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Projet> projets = new ArrayList<>();

    @OneToMany(mappedBy = "investisseur", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Investissement> investissements = new ArrayList<>();

    @OneToMany(mappedBy = "destinataire", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Favori> favoris = new ArrayList<>();

    @OneToMany(mappedBy = "auteur", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Commentaires> commentaires = new ArrayList<>();

    // --- Enums Internes ---
    public enum Role {
        INVESTOR, PROJECT_CREATOR, ADMIN
    }

    public enum StatutCompte {
        ACTIF, SUSPENDU, BANNI
    }

    // --- Helpers Métier ---

    public boolean hasKycValide() {
        return profilKyc != null && profilKyc.isKycValide();
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public boolean isProjectCreator() {
        return this.role == Role.PROJECT_CREATOR;
    }

    public boolean isInvestor() {
        return this.role == Role.INVESTOR;
    }

    public boolean peutInvestir() {
        return this.statut == StatutCompte.ACTIF
                && hasKycValide()
                && (this.role == Role.INVESTOR || this.role == Role.PROJECT_CREATOR);
    }

    public boolean peutCreerProjet() {
        return this.statut == StatutCompte.ACTIF
                && hasKycValide()
                && this.role == Role.PROJECT_CREATOR;
    }

    public boolean peutValiderKyc() {
        return this.statut == StatutCompte.ACTIF && this.role == Role.ADMIN;
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
}