package com.ensias.crowdfunding_project.repositories;

import com.ensias.crowdfunding_project.entities.Utilisateur;
import com.ensias.crowdfunding_project.entities.Utilisateur.Role;
import com.ensias.crowdfunding_project.entities.Utilisateur.StatutCompte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // ── 1. RECHERCHES DE BASE ─────────────────────────────────

    // Recherche par email — login
    Optional<Utilisateur> findByEmail(String email);

    // Vérifier unicité email — inscription
    boolean existsByEmail(String email);

    // Recherche par OAuth — Google / Facebook
    Optional<Utilisateur> findByOauthProviderAndOauthId(
            String oauthProvider, String oauthId
    );

    // Recherche par rôle
    List<Utilisateur> findByRole(Role role);  // ← CORRIGÉ (sans "All")

    // Recherche par statut
    List<Utilisateur> findByStatut(StatutCompte statut);

    // Recherche par email ET rôle — vérification accès
    Optional<Utilisateur> findByEmailAndRole(String email, Role role);

    // ── 2. RECHERCHES AVEC PAGINATION ─────────────────────────

    // Pagination par rôle
    Page<Utilisateur> findByRole(Role role, Pageable pageable);

    // Pagination par statut
    Page<Utilisateur> findByStatut(StatutCompte statut, Pageable pageable);

    // Pagination par rôle ET statut — le plus utilisé
    Page<Utilisateur> findByRoleAndStatut(
            Role role, StatutCompte statut, Pageable pageable
    );

    // ── 3. RECHERCHE PAR OTP ──────────────────────────────────

    @Query("SELECT u FROM Utilisateur u " +
            "WHERE u.email = :email " +
            "AND u.otpCode = :otpCode " +
            "AND u.otpExpiration > :now")
    Optional<Utilisateur> findByEmailAndOtpCodeValide(
            @Param("email") String email,
            @Param("otpCode") String otpCode,
            @Param("now") LocalDateTime now
    );

    // ── 4. RECHERCHE PAR NOM / TEXTE ─────────────────────────

    // Recherche simple — sans pagination
    @Query("SELECT u FROM Utilisateur u WHERE " +
            "LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Utilisateur> searchUsers(@Param("search") String search);

    // Recherche avec pagination
    @Query("SELECT u FROM Utilisateur u WHERE " +
            "LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Utilisateur> searchUsersPaged(
            @Param("search") String search, Pageable pageable
    );

    // ── 5. GESTION KYC ────────────────────────────────────────

    // Utilisateurs avec KYC validé
    @Query("SELECT u FROM Utilisateur u WHERE EXISTS " +
            "(SELECT p FROM ProfilKyc p WHERE p.utilisateur = u " +
            "AND p.kycValide = true)")
    List<Utilisateur> findUsersWithValidKyc();

    // Utilisateurs avec KYC soumis mais non validé
    @Query("SELECT u FROM Utilisateur u WHERE EXISTS " +
            "(SELECT p FROM ProfilKyc p WHERE p.utilisateur = u " +
            "AND p.kycValide = false AND p.kycSoumisAt IS NOT NULL)")
    List<Utilisateur> findUsersWithPendingKyc();

    // ── 6. MISES À JOUR ───────────────────────────────────────

    @Modifying
    @Transactional
    @Query("UPDATE Utilisateur u SET u.statut = :statut WHERE u.id = :id")
    int updateStatut(@Param("id") UUID id, @Param("statut") StatutCompte statut);

    @Modifying
    @Transactional
    @Query("UPDATE Utilisateur u SET u.role = :role WHERE u.id = :id")
    int updateRole(@Param("id") UUID id, @Param("role") Role role);

    @Modifying
    @Transactional
    @Query("UPDATE Utilisateur u SET u.otpCode = null, u.otpExpiration = null WHERE u.id = :id")
    int clearOtp(@Param("id") UUID id);

    @Modifying
    @Transactional
    @Query("UPDATE Utilisateur u SET u.motDePasseHash = :hash WHERE u.id = :id")
    int updateMotDePasse(@Param("id") UUID id, @Param("hash") String hash);

    // ── 7. STATISTIQUES ───────────────────────────────────────

    // Répartition des rôles — dashboard admin
    @Query("SELECT u.role AS role, COUNT(u) AS total " +
            "FROM Utilisateur u GROUP BY u.role")
    List<RoleCount> countUsersByRole();

    // Interface de projection
    interface RoleCount {
        Role getRole();
        Long getTotal();
    }

    // ── 8. MAINTENANCE ────────────────────────────────────────

    // Nettoyage OTP expirés — appelé par un @Scheduled job
    @Modifying
    @Transactional
    @Query("UPDATE Utilisateur u SET u.otpCode = null, u.otpExpiration = null WHERE u.otpExpiration < :now")
    int clearExpiredOtps(@Param("now") LocalDateTime now);
}