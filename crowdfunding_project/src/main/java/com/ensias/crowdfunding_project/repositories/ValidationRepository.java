package com.ensias.crowdfunding_project.repositories;

import com.ensias.crowdfunding_project.entities.Validation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ValidationRepository extends JpaRepository<Validation, UUID> {

    // ── Recherche par projet ──────────────────────────────────

    // Historique complet des décisions pour un projet
    List<Validation> findByProjetIdOrderByDecidedAtDesc(UUID projetId);

    // Dernière décision prise sur un projet
    Optional<Validation> findTopByProjetIdOrderByDecidedAtDesc(UUID projetId);

    // Vérifier si un projet a déjà été approuvé
    boolean existsByProjetIdAndDecision(UUID projetId, String decision);

    // ── Recherche par admin ───────────────────────────────────

    // Toutes les décisions d un admin
    List<Validation> findByAdminIdOrderByDecidedAtDesc(UUID adminId);

    // ── Requêtes personnalisées ───────────────────────────────

    // Dernières validations d un projet avec décision spécifique
    @Query("SELECT v FROM Validation v " +
            "WHERE v.projet.id = :projetId " +
            "AND v.decision = :decision " +
            "ORDER BY v.decidedAt DESC")
    List<Validation> findByProjetIdAndDecision(
            @Param("projetId") UUID projetId,
            @Param("decision") String decision
    );

    // Validations d un admin avec détails projet — évite N+1
    @Query("SELECT v FROM Validation v " +
            "JOIN FETCH v.projet " +
            "WHERE v.admin.id = :adminId " +
            "ORDER BY v.decidedAt DESC")
    List<Validation> findByAdminIdWithProjet(@Param("adminId") UUID adminId);

    // ── Statistiques admin ────────────────────────────────────

    @Query("SELECT COUNT(v) FROM Validation v WHERE v.decision = 'approuve'")
    long countApprobations();

    @Query("SELECT COUNT(v) FROM Validation v WHERE v.decision = 'refuse'")
    long countRefus();
}