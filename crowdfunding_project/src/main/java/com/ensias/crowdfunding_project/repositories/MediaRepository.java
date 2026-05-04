package com.ensias.crowdfunding_project.repositories;

import com.ensias.crowdfunding_project.entities.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface MediaRepository extends JpaRepository<Media, UUID> {

    // ── Recherche par projet ──────────────────────────────────

    // Tous les médias d un projet — triés par date d ajout
    List<Media> findByProjetIdOrderByCreatedAtAsc(UUID projetId);

    // Médias d un projet filtrés par type
    List<Media> findByProjetIdAndTypeMedia(
            UUID projetId,
            String typeMedia
    );

    // ── Recherche par type ────────────────────────────────────

    // Toutes les images d un projet
    @Query("SELECT m FROM Media m " +
            "WHERE m.projet.id = :projetId " +
            "AND m.typeMedia = 'image' " +
            "ORDER BY m.createdAt ASC")
    List<Media> findImagesByProjetId(@Param("projetId") UUID projetId);

    // Toutes les vidéos d un projet
    @Query("SELECT m FROM Media m " +
            "WHERE m.projet.id = :projetId " +
            "AND m.typeMedia = 'video' " +
            "ORDER BY m.createdAt ASC")
    List<Media> findVideosByProjetId(@Param("projetId") UUID projetId);

    // Tous les documents d un projet
    @Query("SELECT m FROM Media m " +
            "WHERE m.projet.id = :projetId " +
            "AND m.typeMedia = 'document' " +
            "ORDER BY m.createdAt ASC")
    List<Media> findDocumentsByProjetId(@Param("projetId") UUID projetId);

    // ── Suppression ───────────────────────────────────────────

    // Supprimer tous les médias d un projet supprimé
    @Modifying
    @Transactional
    void deleteByProjetId(UUID projetId);

    // ── Statistiques ──────────────────────────────────────────

    // Nombre total de médias d un projet
    long countByProjetId(UUID projetId);

    // Nombre de médias par type pour un projet
    long countByProjetIdAndTypeMedia(UUID projetId, String typeMedia);
}