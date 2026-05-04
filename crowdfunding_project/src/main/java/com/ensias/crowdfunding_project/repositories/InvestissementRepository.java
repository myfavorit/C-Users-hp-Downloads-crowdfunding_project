package com.ensias.crowdfunding_project.repositories;

import com.ensias.crowdfunding_project.entities.Investissement;
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

    // ── Recherche par investisseur ────────────────────────────
    List<Investissement> findByInvestisseurIdOrderByCreatedAtDesc(UUID investisseurId);

    List<Investissement> findByInvestisseurIdAndStatutPaiement(
            UUID investisseurId, String statutPaiement
    );

    // ── Recherche par projet ──────────────────────────────────
    List<Investissement> findByProjetIdOrderByCreatedAtDesc(UUID projetId);

    List<Investissement> findByProjetIdAndStatutPaiement(
            UUID projetId, String statutPaiement
    );

    // ── Recherche par référence ───────────────────────────────
    Optional<Investissement> findByReferencePaiement(String referencePaiement);

    // ── Vérifications ─────────────────────────────────────────
    boolean existsByInvestisseurIdAndProjetId(UUID investisseurId, UUID projetId);

    // ── Mise à jour statut paiement ───────────────────────────
    @Modifying
    @Transactional
    @Query("UPDATE Investissement i SET i.statutPaiement = :statut " +
            "WHERE i.id = :id")
    int updateStatutPaiement(
            @Param("id") UUID id,
            @Param("statut") String statut
    );

    // ── Statistiques ──────────────────────────────────────────

    // Montant total investi dans un projet
    @Query("SELECT SUM(i.montant) FROM Investissement i " +
            "WHERE i.projet.id = :projetId " +
            "AND i.statutPaiement = 'valide'")
    BigDecimal sumMontantParProjet(@Param("projetId") UUID projetId);

    // Nombre d investisseurs uniques pour un projet
    @Query("SELECT COUNT(DISTINCT i.investisseur.id) FROM Investissement i " +
            "WHERE i.projet.id = :projetId AND i.statutPaiement = 'valide'")
    long countInvestisseursParProjet(@Param("projetId") UUID projetId);

    // Montant total investi par un utilisateur
    @Query("SELECT SUM(i.montant) FROM Investissement i " +
            "WHERE i.investisseur.id = :investisseurId " +
            "AND i.statutPaiement = 'valide'")
    BigDecimal sumMontantParInvestisseur(@Param("investisseurId") UUID investisseurId);

    // Statistiques globales admin
    @Query("SELECT COUNT(i) FROM Investissement i WHERE i.statutPaiement = 'valide'")
    long countInvestissementsValides();

    @Query("SELECT SUM(i.montant) FROM Investissement i WHERE i.statutPaiement = 'valide'")
    BigDecimal sumMontantTotal();
}