package com.ensias.crowdfunding_project.repositories;

import com.ensias.crowdfunding_project.entities.Investissement;
import com.ensias.crowdfunding_project.entities.Investissement.StatutPaiement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvestissementRepository extends JpaRepository<Investissement, UUID> {

    // PROJECTIONS — interfaces pour les résultats agrégés
    interface ProjetStats {
        UUID getProjetId();
        Long getNbInvestissements();
    }

    interface InvestisseurStats {
        UUID getInvestisseurId();
        BigDecimal getMontantTotal();
    }

    // 1. RECHERCHES PAR INVESTISSEUR

    // Tous les investissements d'un investisseur — plus récents en premier
    List<Investissement> findByInvestisseurIdOrderByCreatedAtDesc(
            UUID investisseurId
    );

    // Investissements d'un investisseur filtrés par statut — triés
    List<Investissement> findByInvestisseurIdAndStatutPaiementOrderByCreatedAtDesc(
            UUID investisseurId,
            StatutPaiement statutPaiement
    );
    // 2. RECHERCHES PAR PROJET

    // Tous les investissements d'un projet — plus récents en premier
    List<Investissement> findByProjetIdOrderByCreatedAtDesc(
            UUID projetId
    );

    // Investissements d'un projet filtrés par statut — triés
    List<Investissement> findByProjetIdAndStatutPaiementOrderByCreatedAtDesc(
            UUID projetId,
            StatutPaiement statutPaiement
    );

    // 3. RECHERCHE PAR RÉFÉRENCE


    // Trouver un investissement par sa référence de paiement
    Optional<Investissement> findByReferencePaiement(String referencePaiement);

    // 4. VÉRIFICATIONS

    // Vérifie si un investisseur a déjà investi dans un projet
    boolean existsByInvestisseurIdAndProjetId(
            UUID investisseurId,
            UUID projetId
    );

    // Vérifie si un investissement avec ce statut existe pour un projet
    boolean existsByProjetIdAndStatutPaiement(
            UUID projetId,
            StatutPaiement statutPaiement
    );

    // 5. MISES À JOUR

    // Mettre à jour le statut d'un investissement
    @Modifying
    @Transactional
    @Query("UPDATE Investissement i SET i.statutPaiement = :statut WHERE i.id = :id")
    int updateStatutPaiement(
            @Param("id") UUID id,
            @Param("statut") StatutPaiement statut
    );

    // 6. STATISTIQUES


    // Montant total investi dans un projet (confirmés uniquement)
    @Query("SELECT COALESCE(SUM(i.montant), 0) FROM Investissement i " +
            "WHERE i.projet.id = :projetId AND i.statutPaiement = 'CONFIRME'")
    BigDecimal sumMontantParProjet(@Param("projetId") UUID projetId);

    // Nombre d'investisseurs uniques pour un projet
    @Query("SELECT COUNT(DISTINCT i.investisseur.id) FROM Investissement i " +
            "WHERE i.projet.id = :projetId AND i.statutPaiement = 'CONFIRME'")
    long countInvestisseursParProjet(@Param("projetId") UUID projetId);

    // Montant total investi par un investisseur (tous projets)
    @Query("SELECT COALESCE(SUM(i.montant), 0) FROM Investissement i " +
            "WHERE i.investisseur.id = :investisseurId AND i.statutPaiement = 'CONFIRME'")
    BigDecimal sumMontantParInvestisseur(
            @Param("investisseurId") UUID investisseurId
    );

    // Compter par statut — typé avec enum
    long countByStatutPaiement(StatutPaiement statutPaiement);

    // Montant total collecté tous projets confondus
    @Query("SELECT COALESCE(SUM(i.montant), 0) FROM Investissement i " +
            "WHERE i.statutPaiement = 'CONFIRME'")
    BigDecimal sumMontantTotal();

    // 7. TOP LISTES — avec projections typées + Pageable

    // Top projets avec le plus d'investissements
    // Usage : PageRequest.of(0, 5) pour le top 5
    @Query("SELECT i.projet.id AS projetId, COUNT(i) AS nbInvestissements " +
            "FROM Investissement i " +
            "WHERE i.statutPaiement = 'CONFIRME' " +
            "GROUP BY i.projet.id " +
            "ORDER BY COUNT(i) DESC")
    Page<ProjetStats> findTopProjetsByInvestissementsCount(Pageable pageable);

    // Top investisseurs par montant total investi
    // Usage : PageRequest.of(0, 5) pour le top 5
    @Query("SELECT i.investisseur.id AS investisseurId, SUM(i.montant) AS montantTotal " +
            "FROM Investissement i " +
            "WHERE i.statutPaiement = 'CONFIRME' " +
            "GROUP BY i.investisseur.id " +
            "ORDER BY SUM(i.montant) DESC")
    Page<InvestisseurStats> findTopInvestisseursByMontant(Pageable pageable);
}