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

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
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
    private Role role = Role.USER;

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
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    // --- Relations (Une seule déclaration par relation !) ---
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

    // --- Enums ---
    public enum Role { USER, ADMIN }
    public enum StatutCompte { ACTIF, SUSPENDU }

    // --- Helpers Métier (Logique de validation) ---

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    /**
     * Vérifie si le KYC est présent dans la table liée et s'il a été validé.
     */
    public boolean hasKycValide() {
        return this.profilKyc != null && this.profilKyc.isKycValide();
    }

    /**
     * Condition pour investir : Compte non suspendu ET identité vérifiée (KYC).
     */
    public boolean peutInvestir() {
        return this.statut == StatutCompte.ACTIF && hasKycValide();
    }

    /**
     * Pour créer un projet, on applique généralement les mêmes règles de sécurité.
     */
    public boolean peutCreerProjet() {
        return peutInvestir();
    }
}