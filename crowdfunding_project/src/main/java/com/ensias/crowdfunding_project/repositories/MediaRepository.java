package com.ensias.crowdfunding_project.repositories;

import com.ensias.crowdfunding_project.entities.Media;
import com.ensias.crowdfunding_project.entities.Media.TypeMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface MediaRepository extends JpaRepository<Media, UUID> {

    /**
     * Tous les médias d'un projet
     */
    List<Media> findByProjetIdOrderByCreatedAtDesc(UUID projetId);

    /**
     * Médias d'un projet par type
     */
    List<Media> findByProjetIdAndTypeMediaOrderByCreatedAtDesc(UUID projetId, TypeMedia typeMedia);

    /**
     * Vérifier si un projet a des médias
     */
    boolean existsByProjetId(UUID projetId);

    /**
     * Compter les médias par type
     */
    long countByProjetIdAndTypeMedia(UUID projetId, TypeMedia typeMedia);

    /**
     * Supprimer tous les médias d'un projet (quand projet supprimé)
     */
    @Modifying
    @Transactional
    void deleteByProjetId(UUID projetId);
}