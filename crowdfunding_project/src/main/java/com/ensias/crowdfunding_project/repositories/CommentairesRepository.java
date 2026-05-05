package com.ensias.crowdfunding_project.repositories;

import com.ensias.crowdfunding_project.entities.Commentaires;
import com.ensias.crowdfunding_project.entities.Commentaires;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentairesRepository extends JpaRepository<Commentaires, UUID> {

    // ============================================================
    // 1. RECHERCHES PAR PROJET
    // ============================================================

    /**
     * Tous les commentaires d'un projet (triés du plus récent au plus ancien)
     */
    List<Commentaires> findByProjetIdOrderByCreatedAtDesc(UUID projetId);

    /**
     * Commentaires d'un projet avec pagination (pour éviter de tout charger)
     */
    Page<Commentaires> findByProjetIdOrderByCreatedAtDesc(UUID projetId, Pageable pageable);

    /**
     * Nombre de commentaires d'un projet
     */
    long countByProjetId(UUID projetId);

    // ============================================================
    // 2. RECHERCHES PAR AUTEUR
    // ============================================================

    /**
     * Tous les commentaires d'un utilisateur (triés du plus récent au plus ancien)
     */
    List<Commentaires> findByAuteurIdOrderByCreatedAtDesc(UUID auteurId);

    // ============================================================
    // 3. SUPPRESSION
    // ============================================================

    /**
     * Supprimer tous les commentaires d'un projet (quand le projet est supprimé)
     */
    @Modifying
    @Transactional
    void deleteByProjetId(UUID projetId);

    /**
     * Supprimer un commentaire spécifique (vérification faite dans le service)
     */
    @Modifying
    @Transactional
    void deleteByIdAndAuteurId(UUID id, UUID auteurId);
}