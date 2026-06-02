package com.ensias.crowdfunding_project.repositories;

import com.ensias.crowdfunding_project.entities.Projet;
import com.ensias.crowdfunding_project.enums.DomaineProjet;
import com.ensias.crowdfunding_project.enums.StatutPaiement;
import com.ensias.crowdfunding_project.enums.StatutProjet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjetRepository extends JpaRepository<Projet, UUID> {

    // 1. RECHERCHES PUBLIQUES (galerie, accès sans authentification)

    @Query("SELECT p FROM Projet p WHERE p.statut = 'VALIDE' AND p.isDeleted = false AND p.dateFin >= :today ORDER BY p.createdAt DESC")
    List<Projet> findProjetsActifs(@Param("today") LocalDate today);

    Optional<Projet> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Projet p WHERE p.id = :id AND p.statut = 'VALIDE' AND p.isDeleted = false AND p.dateFin >= :today")
    boolean estOuvert(@Param("id") UUID id, @Param("today") LocalDate today);

    @Query("SELECT DISTINCT p.domaine FROM Projet p WHERE p.statut = 'VALIDE' AND p.isDeleted = false")
    List<DomaineProjet> findDomainesDistincts();

    @Query("SELECT p FROM Projet p WHERE p.statut = 'VALIDE' AND p.isDeleted = false AND LOWER(p.titre) LIKE LOWER(CONCAT('%', :motCle, '%'))")
    List<Projet> rechercherParTitre(@Param("motCle") String motCle);

    // 2. RECHERCHES POUR LE CRÉATEUR (dashboard créateur)

    List<Projet> findByPorteurIdAndIsDeletedFalse(UUID porteurId);
    List<Projet> findByPorteurIdAndStatutAndIsDeletedFalse(UUID porteurId, StatutProjet statut);
    long countByPorteurId(UUID porteurId);
    long countByPorteurIdAndStatut(UUID porteurId, StatutProjet statut);

    @Query("SELECT COALESCE(SUM(p.montantActuel), 0) FROM Projet p WHERE p.porteur.id = :porteurId AND p.statut = 'VALIDE'")
    BigDecimal sumMontantActuelByPorteurId(@Param("porteurId") UUID porteurId);

    List<Projet> findTop5ByPorteurIdOrderByCreatedAtDesc(UUID porteurId, Pageable pageable);

    // 3. RECHERCHES POUR L'ADMIN (gestion des projets)

    List<Projet> findByStatutAndIsDeletedFalse(StatutProjet statut);
    Page<Projet> findByStatutAndIsDeletedFalse(StatutProjet statut, Pageable pageable);
    long countByStatutAndIsDeletedFalse(StatutProjet statut);

    // 4. MISES À JOUR (écriture)

    @Modifying
    @Transactional
    @Query("UPDATE Projet p SET p.statut = 'VALIDE', p.dateDebut = :dateDebut, p.dateFin = :dateFin WHERE p.id = :id AND p.statut = 'EN_ATTENTE'")
    int valider(@Param("id") UUID id, @Param("dateDebut") LocalDate dateDebut, @Param("dateFin") LocalDate dateFin);

    @Modifying
    @Transactional
    @Query("UPDATE Projet p SET p.statut = 'REJETE' WHERE p.id = :id AND p.statut = 'EN_ATTENTE'")
    int refuser(@Param("id") UUID id);

    @Modifying
    @Transactional
    @Query("UPDATE Projet p SET p.montantActuel = p.montantActuel + :montant WHERE p.id = :id AND p.statut = 'VALIDE' AND p.dateFin >= :today")
    int ajouterInvestissement(@Param("id") UUID id, @Param("montant") BigDecimal montant, @Param("today") LocalDate today);

    @Modifying
    @Transactional
    @Query("UPDATE Projet p SET p.isDeleted = true WHERE p.id = :id")
    int softDelete(@Param("id") UUID id);

    // 5. STATISTIQUES GLOBALES (dashboard admin)

    @Query("SELECT COALESCE(SUM(p.montantActuel), 0) FROM Projet p WHERE p.isDeleted = false AND p.statut = 'VALIDE'")
    BigDecimal montantTotalCollecte();

    // 6. LISTES RÉCENTES (dashboard admin)

    Page<Projet> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 7. VÉRIFICATIONS POUR LA SUPPRESSION DE COMPTE (UserProfileService)

    boolean existsByPorteurIdAndStatutAndMontantActuelGreaterThan(UUID porteurId, StatutProjet statut, BigDecimal montant);
    // À AJOUTER dans ProjetRepository.java
// ============================================================

    /**
     * Évolution mensuelle du montant collecté sur les projets du créateur.
     * Retourne : [mois(int), année(int), montantCollecte(BigDecimal)]
     */
    @Query("""
    SELECT
        MONTH(i.createdAt)          AS mois,
        YEAR(i.createdAt)           AS annee,
        SUM(i.montant)              AS montantCollecte,
        COUNT(i.id)                 AS nombreInvestissements
    FROM Investissement i
    JOIN i.projet p
    WHERE p.porteur.id = :porteurId
      AND i.statutPaiement = :statut
    GROUP BY YEAR(i.createdAt), MONTH(i.createdAt)
    ORDER BY YEAR(i.createdAt) ASC, MONTH(i.createdAt) ASC
    """)
    List<Object[]> findEvolutionCollecteMensuelle(
            @Param("porteurId") UUID porteurId,
            @Param("statut") StatutPaiement statut);


    /**
     * Top 5 projets du créateur triés par date de création décroissante,
     * avec le NOMBRE d'investissements reçus par projet.
     * Retourne : [projet(Projet), nombreInvestissements(long)]
     */
    @Query("""
    SELECT p, COUNT(i.id)
    FROM Projet p
    LEFT JOIN Investissement i
          ON i.projet = p
         AND i.statutPaiement = com.ensias.crowdfunding_project.enums.StatutPaiement.CONFIRME
    WHERE p.porteur.id = :porteurId
    GROUP BY p
    ORDER BY p.createdAt DESC
    """)
    List<Object[]> findTop5ByPorteurIdWithInvestCount(
            @Param("porteurId") UUID porteurId,
            Pageable pageable);
}