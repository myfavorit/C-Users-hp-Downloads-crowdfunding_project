package com.ensias.crowdfunding_project.repositories;

import com.ensias.crowdfunding_project.entities.Validation;
import com.ensias.crowdfunding_project.entities.Validation.Decision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ValidationRepository extends JpaRepository<Validation, UUID> {

    // ============================================================
    // 1. RECHERCHES PAR PROJET
    // ============================================================

    /**
     * Dernière décision prise sur un projet
     */
    Optional<Validation> findTopByProjetIdOrderByDecidedAtDesc(UUID projetId);

    /**
     * Vérifier si un projet a déjà été validé
     */
    boolean existsByProjetIdAndDecision(UUID projetId, Decision decision);

    /**
     * Vérifier si un projet a une validation (peu importe la décision)
     */
    boolean existsByProjetId(UUID projetId);

    /**
     * Trouver la validation d'un projet avec détails admin et projet (évite N+1)
     */
    @Query("SELECT v FROM Validation v " +
            "JOIN FETCH v.admin " +
            "JOIN FETCH v.projet " +
            "WHERE v.projet.id = :projetId")
    Optional<Validation> findByProjetIdWithDetails(@Param("projetId") UUID projetId);

    // ============================================================
    // 2. RECHERCHES PAR ADMIN
    // ============================================================

    /**
     * Toutes les décisions d'un admin (triées du plus récent au plus ancien)
     */
    List<Validation> findByAdminIdOrderByDecidedAtDesc(UUID adminId);

    /**
     * Décisions d'un admin avec détails projet (évite N+1)
     */
    @Query("SELECT v FROM Validation v JOIN FETCH v.projet WHERE v.admin.id = :adminId ORDER BY v.decidedAt DESC")
    List<Validation> findByAdminIdWithProjet(@Param("adminId") UUID adminId);

    // ============================================================
    // 3. STATISTIQUES (Dashboard Admin)
    // ============================================================

    /**
     * Nombre total de validations par décision
     */
    long countByDecision(Decision decision);

    /**
     * Taux d'approbation des projets (protégé contre division par zéro)
     */
    @Query("SELECT CASE WHEN COUNT(v) = 0 THEN 0.0 " +
            "ELSE CAST(COUNT(CASE WHEN v.decision = 'VALIDE' THEN 1 END) AS double) / " +
            "CAST(COUNT(v) AS double) * 100 END " +
            "FROM Validation v")
    Double getTauxApprobation();
}