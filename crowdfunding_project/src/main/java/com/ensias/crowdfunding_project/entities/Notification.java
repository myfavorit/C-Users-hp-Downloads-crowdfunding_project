package com.ensias.crowdfunding_project.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "NOTIFICATION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "titre", "type", "lu", "createdAt"})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_id", nullable = false, columnDefinition = "BINARY(16)")
    @ToString.Exclude
    private Utilisateur destinataire;

    @Column(nullable = false, length = 255)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TypeNotification type;

    @Column(nullable = false)
    @Builder.Default
    private boolean lu = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Enum ─────────────────────────────────────────────────────────────

    public enum TypeNotification {
        PROJET_VALIDE,
        PROJET_REJETE,
        INVESTISSEMENT_RECU,
        KYC_VALIDE,
        KYC_REJETE,
        REMBOURSEMENT,
        SYSTEME
    }

    // ── Méthodes métier ─────────────────────────────────────────────────

    /**
     * Marquer la notification comme lue
     */
    public void marquerCommeLue() {
        this.lu = true;
    }

    /**
     * Vérifie si la notification est lue
     */
    public boolean estLue() {
        return this.lu;
    }

    /**
     * Créer une notification de validation de projet
     */
    public static Notification projetValide(Utilisateur destinataire, String titreProjet) {
        return Notification.builder()
                .destinataire(destinataire)
                .titre("Projet validé")
                .message("Votre projet \"" + titreProjet + "\" a été validé et est maintenant visible par les investisseurs.")
                .type(TypeNotification.PROJET_VALIDE)
                .build();
    }

    /**
     * Créer une notification de rejet de projet
     */
    public static Notification projetRejete(Utilisateur destinataire, String titreProjet, String motif) {
        return Notification.builder()
                .destinataire(destinataire)
                .titre("Projet refusé")
                .message("Votre projet \"" + titreProjet + "\" a été refusé. Motif : " + motif)
                .type(TypeNotification.PROJET_REJETE)
                .build();
    }

    /**
     * Créer une notification d'investissement reçu
     */
    public static Notification investissementRecu(Utilisateur destinataire, String titreProjet, String montant, String investisseur) {
        return Notification.builder()
                .destinataire(destinataire)
                .titre("💰 Nouvel investissement")
                .message(investisseur + " a investi " + montant + "€ dans votre projet \"" + titreProjet + "\"")
                .type(TypeNotification.INVESTISSEMENT_RECU)
                .build();
    }

    /**
     * Créer une notification de KYC validé
     */
    public static Notification kycValide(Utilisateur destinataire) {
        return Notification.builder()
                .destinataire(destinataire)
                .titre("✅ KYC validé")
                .message("Votre dossier KYC a été validé. Vous pouvez maintenant investir et créer des projets.")
                .type(TypeNotification.KYC_VALIDE)
                .build();
    }

    /**
     * Créer une notification de KYC rejeté
     */
    public static Notification kycRejete(Utilisateur destinataire, String motif) {
        return Notification.builder()
                .destinataire(destinataire)
                .titre("KYC refusé")
                .message("Votre dossier KYC a été refusé. Motif : " + motif + ". Veuillez soumettre un nouveau dossier.")
                .type(TypeNotification.KYC_REJETE)
                .build();
    }
}