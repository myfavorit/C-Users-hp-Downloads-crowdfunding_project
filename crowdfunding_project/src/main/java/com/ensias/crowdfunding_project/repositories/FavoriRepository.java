package com.ensias.crowdfunding_project.repositories;

import com.ensias.crowdfunding_project.entities.Favori;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriRepository extends JpaRepository<Favori, UUID> {

    /**
     * Récupérer les favoris d'un utilisateur
     */
    List<Favori> findByUtilisateurIdOrderByCreatedAtDesc(UUID utilisateurId);

    /**
     * Récupérer les favoris d'un utilisateur avec détails projet (évite N+1)
     */
    @Query("SELECT f FROM Favori f JOIN FETCH f.projet WHERE f.utilisateur.id = :utilisateurId ORDER BY f.createdAt DESC")
    List<Favori> findByUtilisateurIdWithProjet(@Param("utilisateurId") UUID utilisateurId);

    /**
     * Vérifier si un projet est favori
     */
    boolean existsByUtilisateurIdAndProjetId(UUID utilisateurId, UUID projetId);

    /**
     * Trouver un favori spécifique
     */
    Optional<Favori> findByUtilisateurIdAndProjetId(UUID utilisateurId, UUID projetId);

    /**
     * Compter les favoris d'un projet
     */
    long countByProjetId(UUID projetId);

    /**
     * Supprimer tous les favoris d'un projet (quand projet supprimé)
     */
    void deleteByProjetId(UUID projetId);

    /**
     * Supprimer tous les favoris d'un utilisateur (quand compte supprimé)
     */
    void deleteByUtilisateurId(UUID utilisateurId);
}