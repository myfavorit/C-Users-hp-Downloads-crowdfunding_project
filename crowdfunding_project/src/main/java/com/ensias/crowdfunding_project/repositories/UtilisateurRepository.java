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

    // 1. RECHERCHES DE BASE (5 méthodes)

    // Recherche par email (login)
    Optional<Utilisateur> findByEmail(String email);

    // Vérifier unicité email (inscription)
    boolean existsByEmail(String email);

    // Recherche par OAuth (Google/Facebook)
    Optional<Utilisateur> findByOauthProviderAndOauthId(String oauthProvider, String oauthId);

    // Recherche par rôle
    List<Utilisateur> findByRole(Role role);

    // Recherche par statut
    List<Utilisateur> findByStatut(StatutCompte statut);

    // 2. RECHERCHES AVEC PAGINATION (4 méthodes)

    // Pagination par rôle
    Page<Utilisateur> findByRole(Role role, Pageable pageable);

    // Pagination par statut
    Page<Utilisateur> findByStatut(StatutCompte statut, Pageable pageable);

    // Pagination par rôle ET statut (le plus utilisé)
    Page<Utilisateur> findByRoleAndStatut(Role role, StatutCompte statut, Pageable pageable);

    // 3. RECHERCHE PAR OTP (1 méthode)

    @Query("SELECT u FROM Utilisateur u " +
            "WHERE u.email = :email " +
            "AND u.otpCode = :otpCode " +
            "AND u.otpExpiration > :now")
    Optional<Utilisateur> findByEmailAndOtpCodeValide(
            @Param("email") String email,
            @Param("otpCode") String otpCode,
            @Param("now") LocalDateTime now
    );
    // 4. RECHERCHE PAR NOM/TEXT (barre de recherche) - 2 méthodes

    // Recherche simple (sans pagination)
    @Query("SELECT u FROM Utilisateur u WHERE " +
            "LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Utilisateur> searchUsers(@Param("search") String search);

    // Recherche AVEC pagination
    @Query("SELECT u FROM Utilisateur u WHERE " +
            "LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Utilisateur> searchUsersPaged(@Param("search") String search, Pageable pageable);

    // 5. GESTION KYC (2 méthodes)

    // Utilisateurs avec KYC validé
    @Query("SELECT u FROM Utilisateur u WHERE u.profilKyc IS NOT NULL AND u.profilKyc.kycValide = true")
    List<Utilisateur> findUsersWithValidKyc();

    // Utilisateurs avec KYC en attente (soumis mais non validé)
    @Query("SELECT u FROM Utilisateur u WHERE u.profilKyc IS NOT NULL AND u.profilKyc.kycValide = false")
    List<Utilisateur> findUsersWithPendingKyc();

    // 6. MISES À JOUR (4 méthodes)

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

    // 7. STATISTIQUES (2 méthodes)
    // Répartition des rôles (dashboard)
    @Query("SELECT u.role, COUNT(u) FROM Utilisateur u GROUP BY u.role")
    List<Object[]> countUsersByRole();

    // Nettoyage OTP expirés (job programmé)
    @Modifying
    @Transactional
    @Query("UPDATE Utilisateur u SET u.otpCode = null, u.otpExpiration = null WHERE u.otpExpiration < :now")
    int clearExpiredOtps(@Param("now") LocalDateTime now);
}