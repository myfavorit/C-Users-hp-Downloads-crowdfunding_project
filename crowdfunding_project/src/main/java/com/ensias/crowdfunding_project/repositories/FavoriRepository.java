package com.ensias.crowdfunding_project.repositories;

import com.ensias.crowdfunding_project.entities.Favori;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriRepository extends JpaRepository<Favori, UUID> {

    // ── Recherche par Utilisateur ──────────────────────────────

    // Liste tous les favoris d'un utilisateur (les plus récents en premier)
    List<Favori> findByUtilisateurIdOrderByCreatedAtDesc(UUID utilisateurId);

    // Vérifier si un projet spécifique est déjà en favori pour un utilisateur
    Optional<Favori> findByUtilisateurIdAndProjetId(UUID utilisateurId, UUID projetId);

    boolean existsByUtilisateurIdAndProjetId(UUID utilisateurId, UUID projetId);

    // ── Recherche par Projet ──────────────────────────────────

    // Trouver tous les utilisateurs qui ont mis ce projet en favori
    List<Favori> findByProjetId(UUID projetId);

    // ── Actions de Suppression (Retirer un favori) ─────────────

    @Modifying
    @Transactional
    @Query("DELETE FROM Favori f WHERE f.utilisateur.id = :userId AND f.projet.id = :projetId")
    void removeByUserIdAndProjetId(@Param("userId") UUID userId, @Param("projetId") UUID projetId);

    @Modifying
    @Transactional
    void deleteByProjetId(UUID projetId); // Utile si un projet est supprimé

    // ── Statistiques ──────────────────────────────────────────

    // Nombre de personnes ayant mis ce projet en favori (Popularité)
    long countByProjetId(UUID projetId);

    // Nombre de favoris d'un utilisateur
    long countByUtilisateurId(UUID utilisateurId);
}