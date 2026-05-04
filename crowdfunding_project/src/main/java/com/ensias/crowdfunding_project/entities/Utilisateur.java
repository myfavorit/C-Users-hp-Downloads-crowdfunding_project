package com.ensias.crowdfunding_project.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "mot_de_passe_hash")
    private String motDePasseHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private Role role = Role.INVESTOR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private StatutCompte statut = StatutCompte.ACTIF;

    // --- OTP & OAuth ---
    @Column(name = "otp_code")
    private String otpCode;

    @Column(name = "otp_expiration")
    private LocalDateTime otpExpiration;

    @Column(name = "oauth_provider")
    private String oauthProvider;

    @Column(name = "oauth_id")
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

    // --- Relations ---
    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ProfilKyc profilKyc;

    @OneToMany(mappedBy = "porteur")
    private List<Projet> projets;

    @OneToMany(mappedBy = "investisseur")
    private List<Investissement> investissements;

    @OneToMany(mappedBy = "destinataire", cascade = CascadeType.ALL)
    private List<Notification> notifications;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private List<Favori> favoris;

    @OneToMany(mappedBy = "auteur")
    private List<Commentaires> commentaires;

    // --- Enums Internes ---
    public enum Role {
        INVESTOR,
        PROJECT_CREATOR,
        ADMIN
    }

    public enum StatutCompte {
        ACTIF,
        SUSPENDU,
        BANNI
    }

    // --- Helpers Métier ---

    /**
     * Vérifie si l'utilisateur a un profil KYC validé
     */
    private boolean hasKycValide() {
        return profilKyc != null && profilKyc.isKycValide();
    }

    /**
     * Vérifie si l'utilisateur est un administrateur
     */
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    /**
     * Vérifie si l'utilisateur peut investir dans un projet
     * Conditions : compte actif + KYC validé + (INVESTOR ou PROJECT_CREATOR)
     */
    public boolean peutInvestir() {
        return this.statut == StatutCompte.ACTIF
                && hasKycValide()
                && (this.role == Role.INVESTOR || this.role == Role.PROJECT_CREATOR);
    }

    /**
     * Vérifie si l'utilisateur peut créer un projet
     * Conditions : compte actif + KYC validé + ROLE PROJECT_CREATOR uniquement
     */
    public boolean peutCreerProjet() {
        return this.statut == StatutCompte.ACTIF
                && hasKycValide()
                && this.role == Role.PROJECT_CREATOR;
    }

    /**
     * Vérifie si l'utilisateur peut valider des KYC (seul l'admin)
     */
    public boolean peutValiderKyc() {
        return this.statut == StatutCompte.ACTIF && this.role == Role.ADMIN;
    }

    /**
     * Vérifie si le profil KYC est soumis mais pas encore validé
     */
    public boolean isKycEnAttente() {
        return profilKyc != null && !profilKyc.isKycValide();
    }

    /**
     * Vérifie si l'utilisateur a soumis son KYC
     */
    public boolean aSoumisKyc() {
        return profilKyc != null;
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