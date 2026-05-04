package com.ensias.crowdfunding_project.repositories;

import com.ensias.crowdfunding_project.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {

    // ── Recherche par email ───────────────────────────────────

    // Pour login
    Optional<Utilisateur> findByEmail(String email);

    // Pour inscription — vérifier unicité
    boolean existsByEmail(String email);

    // ── Recherche par OAuth ───────────────────────────────────

    // Pour login Google / Facebook
    Optional<Utilisateur> findByOauthProviderAndOauthId(
            String oauthProvider,
            String oauthId
    );

    // ── Recherche par OTP ─────────────────────────────────────

    // Recherche OTP avec vérification expiration
    @Query("SELECT u FROM Utilisateur u " +
            "WHERE u.email = :email " +
            "AND u.otpCode = :otpCode " +
            "AND u.otpExpiration > :now")
    Optional<Utilisateur> findByEmailAndOtpCodeValide(
            @Param("email") String email,
            @Param("otpCode") String otpCode,
            @Param("now") LocalDateTime now
    );

    // ── Recherche par rôle ────────────────────────────────────

    // role : INVESTOR | PROJECT_CREATOR | ADMIN
    List<Utilisateur> findByRole(String role);

    // ── Recherche par statut ──────────────────────────────────

    // statut : actif | suspendu | banni
    List<Utilisateur> findByStatut(String statut);

    // ── Mises à jour ─────────────────────────────────────────

    // Changer le statut d'un utilisateur (actif/suspendu/banni)
    @Modifying
    @Transactional
    @Query("UPDATE Utilisateur u SET u.statut = :statut WHERE u.id = :id")
    int updateStatut(@Param("id") UUID id, @Param("statut") String statut);

    // Effacer OTP après validation
    @Modifying
    @Transactional
    @Query("UPDATE Utilisateur u SET u.otpCode = null, " +
            "u.otpExpiration = null WHERE u.id = :id")
    int clearOtp(@Param("id") UUID id);

    // Reset mot de passe
    @Modifying
    @Transactional
    @Query("UPDATE Utilisateur u SET u.motDePasseHash = :hash " +
            "WHERE u.id = :id")
    int updateMotDePasse(@Param("id") UUID id, @Param("hash") String hash);

    // ── Statistiques ──────────────────────────────────────────

    // Compter par rôle — dashboard admin
    long countByRole(String role);

    // Compter par statut — dashboard admin
    long countByStatut(String statut);
}