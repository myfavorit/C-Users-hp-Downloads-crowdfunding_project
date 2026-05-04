package com.ensias.crowdfunding_project.repositories;

import com.ensias.crowdfunding_project.entities.Projet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjetRepository extends JpaRepository<Projet, UUID> {

    // ── Recherche par porteur ─────────────────────────────────
    List<Projet> findByPorteurIdAndIsDeletedFalse(UUID porteurId);

    List<Projet> findByPorteurIdAndStatutAndIsDeletedFalse(
            UUID porteurId, String statut
    );

    // ── Recherche par statut ──────────────────────────────────
    List<Projet> findByStatutAndIsDeletedFalse(String statut);

    // Projets actifs non expirés — galerie page d accueil
    @Query("SELECT p FROM Projet p WHERE p.statut = 'actif' " +
            "AND p.isDeleted = false " +
            "AND p.dateFin >= :today " +
            "ORDER BY p.createdAt DESC")
    List<Projet> findProjetsActifs(@Param("today") LocalDate today);

    // ── Recherche par domaine ─────────────────────────────────
    @Query("SELECT p FROM Projet p WHERE p.statut = 'actif' " +
            "AND p.isDeleted = false " +
            "AND LOWER(p.domaine) = LOWER(:domaine) " +
            "AND p.dateFin >= :today")
    List<Projet> findByDomaine(
            @Param("domaine") String domaine,
            @Param("today") LocalDate today
    );

    // ── Recherche par titre ───────────────────────────────────
    @Query("SELECT p FROM Projet p WHERE p.statut = 'actif' " +
            "AND p.isDeleted = false " +
            "AND LOWER(p.titre) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Projet> searchByTitre(@Param("keyword") String keyword);

    // ── Vérification timer — diagramme 5 ─────────────────────
    @Query("SELECT p FROM Projet p WHERE p.id = :id " +
            "AND p.statut = 'actif' " +
            "AND p.isDeleted = false " +
            "AND p.dateFin >= :today")
    Optional<Projet> findProjetActifNonExpire(
            @Param("id") UUID id,
            @Param("today") LocalDate today
    );

    // ── Mises à jour ──────────────────────────────────────────

    // Validation admin — activer le projet avec dates
    @Modifying
    @Transactional
    @Query("UPDATE Projet p SET p.statut = :statut, " +
            "p.dateDebut = :dateDebut, p.dateFin = :dateFin " +
            "WHERE p.id = :id")
    int updateStatutEtDates(
            @Param("id") UUID id,
            @Param("statut") String statut,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin
    );

    // Refus admin — changer statut uniquement
    @Modifying
    @Transactional
    @Query("UPDATE Projet p SET p.statut = :statut WHERE p.id = :id")
    int updateStatut(@Param("id") UUID id, @Param("statut") String statut);

    // Incrémenter montant_actuel après investissement validé
    @Modifying
    @Transactional
    @Query("UPDATE Projet p SET p.montantActuel = p.montantActuel + :montant " +
            "WHERE p.id = :id")
    int incrementerMontantActuel(
            @Param("id") UUID id,
            @Param("montant") BigDecimal montant
    );

    // Suppression logique
    @Modifying
    @Transactional
    @Query("UPDATE Projet p SET p.isDeleted = true WHERE p.id = :id")
    int softDelete(@Param("id") UUID id);

    // ── Statistiques ──────────────────────────────────────────
    long countByStatutAndIsDeletedFalse(String statut);

    @Query("SELECT COUNT(p) FROM Projet p WHERE p.statut = 'actif' " +
            "AND p.isDeleted = false AND p.dateFin >= :today")
    long countProjetsActifs(@Param("today") LocalDate today);

    @Query("SELECT SUM(p.montantActuel) FROM Projet p WHERE p.isDeleted = false")
    BigDecimal sumMontantTotal();

    // Liste des domaines distincts — pour filtrage galerie
    @Query("SELECT DISTINCT p.domaine FROM Projet p " +
            "WHERE p.statut = 'actif' AND p.isDeleted = false")
    List<String> findDomainesDistincts();
}