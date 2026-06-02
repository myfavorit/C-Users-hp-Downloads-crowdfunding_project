package com.ensias.crowdfunding_project.repositories;

import com.ensias.crowdfunding_project.entities.Projet;
import com.ensias.crowdfunding_project.enums.DomaineProjet;
import com.ensias.crowdfunding_project.enums.StatutProjet;
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

    // ============================================================
    // RECHERCHES UTILISATEUR (galerie)
    // ============================================================

    /**
     * Projets actifs pour la galerie (VALIDE + non expiré + non supprimé)
     */
    @Query("SELECT p FROM Projet p WHERE p.statut = 'VALIDE' " +
            "AND p.isDeleted = false AND p.dateFin >= :today ORDER BY p.createdAt DESC")
    List<Projet> findProjetsActifs(@Param("today") LocalDate today);

    /**
     * Détail d'un projet (avec vérification existence)
     */
    Optional<Projet> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Vérifier si un projet est ouvert avant investissement
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Projet p " +
            "WHERE p.id = :id AND p.statut = 'VALIDE' AND p.isDeleted = false AND p.dateFin >= :today")
    boolean estOuvert(@Param("id") UUID id, @Param("today") LocalDate today);

    // ============================================================
    // RECHERCHES CRÉATEUR (dashboard)
    // ============================================================

    /**
     * Tous les projets du créateur (non supprimés)
     */
    List<Projet> findByPorteurIdAndIsDeletedFalse(UUID porteurId);

    /**
     * Projets du créateur par statut (pour filtrage)
     */
    List<Projet> findByPorteurIdAndStatutAndIsDeletedFalse(UUID porteurId, StatutProjet statut);

    // ============================================================
    // RECHERCHES ADMIN (gestion des projets)
    // ============================================================

    /**
     * Projets en attente de validation (statut = EN_ATTENTE)
     */
    List<Projet> findByStatutAndIsDeletedFalse(StatutProjet statut);

    // ============================================================
    // BARRE DE RECHERCHE (galerie)
    // ============================================================

    @Query("SELECT p FROM Projet p WHERE p.statut = 'VALIDE' AND p.isDeleted = false " +
            "AND LOWER(p.titre) LIKE LOWER(CONCAT('%', :motCle, '%'))")
    List<Projet> rechercherParTitre(@Param("motCle") String motCle);

    // ============================================================
    // MISES À JOUR (transactionnelles)
    // ============================================================

    /**
     * Admin valide un projet -> passe VALIDE + initialise dates
     */
    @Modifying
    @Transactional
    @Query("UPDATE Projet p SET p.statut = 'VALIDE', p.dateDebut = :dateDebut, p.dateFin = :dateFin " +
            "WHERE p.id = :id AND p.statut = 'EN_ATTENTE'")
    int valider(@Param("id") UUID id,
                @Param("dateDebut") LocalDate dateDebut,
                @Param("dateFin") LocalDate dateFin);

    /**
     * Admin refuse un projet
     */
    @Modifying
    @Transactional
    @Query("UPDATE Projet p SET p.statut = 'REJETE' WHERE p.id = :id AND p.statut = 'EN_ATTENTE'")
    int refuser(@Param("id") UUID id);

    /**
     * Ajouter un investissement (incrémente montant + clôture auto si objectif atteint)
     */
    @Modifying
    @Transactional
    @Query("UPDATE Projet p SET p.montantActuel = p.montantActuel + :montant " +
            "WHERE p.id = :id AND p.statut = 'VALIDE' AND p.dateFin >= :today")
    int ajouterInvestissement(@Param("id") UUID id,
                              @Param("montant") BigDecimal montant,
                              @Param("today") LocalDate today);

    /**
     * Soft delete - suppression logique
     */
    @Modifying
    @Transactional
    @Query("UPDATE Projet p SET p.isDeleted = true WHERE p.id = :id")
    int softDelete(@Param("id") UUID id);

    // ============================================================
    // STATISTIQUES (dashboard admin)
    // ============================================================

    /**
     * Nombre total par statut
     */
    long countByStatutAndIsDeletedFalse(StatutProjet statut);

    /**
     * Montant total collecté
     */
    @Query("SELECT COALESCE(SUM(p.montantActuel), 0) FROM Projet p WHERE p.isDeleted = false")
    BigDecimal montantTotalCollecte();

    /**
     * Liste des domaines pour filtrage
     */
    @Query("SELECT DISTINCT p.domaine FROM Projet p WHERE p.statut = 'VALIDE' AND p.isDeleted = false")
    List<DomaineProjet> findDomainesDistincts();
}