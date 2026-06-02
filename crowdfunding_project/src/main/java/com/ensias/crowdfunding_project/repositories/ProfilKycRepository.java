package com.ensias.crowdfunding_project.repositories;

import com.ensias.crowdfunding_project.entities.ProfilKyc;
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
public interface ProfilKycRepository extends JpaRepository<ProfilKyc, UUID> {

    // ── RECHERCHES DE BASE ───────────────────────────────────────────────

    Optional<ProfilKyc> findByUtilisateurId(UUID utilisateurId);

    boolean existsByUtilisateurId(UUID utilisateurId);

    boolean existsByUtilisateurIdAndKycValide(UUID utilisateurId, boolean kycValide);

    // ── RECHERCHES POUR ADMIN (UNIQUEMENT UTILISATEURS ACTIFS) ────────────

    List<ProfilKyc> findByKycValideTrue();

    @Query("SELECT p FROM ProfilKyc p WHERE p.kycValide = false AND p.kycSoumisAt IS NOT NULL AND p.utilisateur.statut = 'ACTIF'")
    List<ProfilKyc> findPendingKycForActiveUsers();

    @Query("SELECT p FROM ProfilKyc p JOIN FETCH p.utilisateur WHERE p.kycValide = false AND p.kycSoumisAt IS NOT NULL AND p.utilisateur.statut = 'ACTIF'")
    List<ProfilKyc> findPendingKycWithUtilisateurForActiveUsers();

    // ── STATISTIQUES (UNIQUEMENT UTILISATEURS ACTIFS) ─────────────────────

    long countByKycValideTrue();

    @Query("SELECT COUNT(p) FROM ProfilKyc p WHERE p.kycValide = false AND p.kycSoumisAt IS NOT NULL AND p.utilisateur.statut = 'ACTIF'")
    long countKycEnAttenteForActiveUsers();

    @Query("SELECT COUNT(p) FROM ProfilKyc p WHERE p.kycValide = true AND p.utilisateur.statut = 'ACTIF'")
    long countValidKycForActiveUsers();

    @Query("SELECT p.kycValide, COUNT(p) FROM ProfilKyc p WHERE p.utilisateur.statut = 'ACTIF' GROUP BY p.kycValide")
    List<Object[]> countByStatutForActiveUsers();

    // ── VÉRIFICATIONS SPÉCIFIQUES ────────────────────────────────────────

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProfilKyc p WHERE p.utilisateur.id = :userId AND p.kycValide = false AND p.kycSoumisAt IS NOT NULL")
    boolean hasPendingKyc(@Param("userId") UUID userId);

    @Query("SELECT CASE WHEN COUNT(p) = 0 THEN true ELSE false END FROM ProfilKyc p WHERE p.utilisateur.id = :userId AND (p.kycValide = true OR p.kycSoumisAt IS NOT NULL)")
    boolean peutSoumettreKyc(@Param("userId") UUID userId);

    // ── MAINTENANCE ──────────────────────────────────────────────────────

    @Modifying
    @Transactional
    @Query("UPDATE ProfilKyc p SET p.kycValide = false, p.kycValideAt = null, p.kycSoumisAt = null WHERE p.utilisateur.id = :utilisateurId")
    int resetKyc(@Param("utilisateurId") UUID utilisateurId);
}