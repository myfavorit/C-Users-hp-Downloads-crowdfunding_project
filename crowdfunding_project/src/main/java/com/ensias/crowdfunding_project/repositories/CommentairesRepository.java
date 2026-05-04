package com.ensias.crowdfunding_project.repositories;

import com.ensias.crowdfunding_project.entities.Commentaires;
import com.ensias.crowdfunding_project.entities.Commentaires;
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

    // Tous les commentaires d un projet — fiche projet
    List<Commentaires> findByProjetIdOrderByCreatedAtDesc(UUID projetId);

    // Tous les commentaires d un utilisateur
    List<Commentaires> findByAuteurIdOrderByCreatedAtDesc(UUID auteurId);

    // Compter les commentaires d un projet
    long countByProjetId(UUID projetId);

    // Supprimer tous les commentaires d un projet
    @Modifying
    @Transactional
    void deleteByProjetId(UUID projetId);
}