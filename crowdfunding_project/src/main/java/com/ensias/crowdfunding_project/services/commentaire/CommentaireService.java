package com.ensias.crowdfunding_project.services.commentaire;


import com.ensias.crowdfunding_project.entities.Commentaires;
import com.ensias.crowdfunding_project.entities.Projet;
import com.ensias.crowdfunding_project.entities.Utilisateur;
import com.ensias.crowdfunding_project.repositories.CommentairesRepository;
import com.ensias.crowdfunding_project.repositories.ProjetRepository;
import com.ensias.crowdfunding_project.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentaireService {

    private final CommentairesRepository commentairesRepository;
    private final ProjetRepository projetRepository;
    private final UtilisateurRepository utilisateurRepository;

    // ============================================================
    // 1. AJOUTER UN COMMENTAIRE
    // ============================================================

    @Transactional
    public Commentaires ajouterCommentaire(UUID projetId, UUID auteurId, String contenu) {

        // 1. Find project
        Projet projet = projetRepository.findByIdAndIsDeletedFalse(projetId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        // 2. Find author
        Utilisateur auteur = utilisateurRepository.findById(auteurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // 3. Validate content
        if (contenu == null || contenu.isBlank()) {
            throw new IllegalArgumentException("Le contenu ne peut pas être vide");
        }
        if (contenu.length() > 2000) {
            throw new IllegalArgumentException("Le contenu ne peut pas dépasser 2000 caractères");
        }

        // 4. Build and save
        Commentaires commentaire = Commentaires.builder()
                .projet(projet)
                .auteur(auteur)
                .contenu(contenu.trim())
                .build();

        log.info("Commentaire ajouté par {} sur le projet {}", auteurId, projetId);
        return commentairesRepository.save(commentaire);
    }

    // ============================================================
    // 2. LISTER LES COMMENTAIRES D'UN PROJET
    // ============================================================

    @Transactional(readOnly = true)
    public List<Commentaires> getCommentairesParProjet(UUID projetId) {
        return commentairesRepository.findByProjetIdOrderByCreatedAtDesc(projetId);
    }

    // ============================================================
    // 3. SUPPRIMER UN COMMENTAIRE
    // ============================================================

    @Transactional
    public void supprimerCommentaire(UUID commentaireId, UUID utilisateurId) {

        Commentaires commentaire = commentairesRepository.findById(commentaireId)
                .orElseThrow(() -> new RuntimeException("Commentaire introuvable"));

        // Only the author can delete their comment
        if (!commentaire.estAuteur(utilisateurId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer ce commentaire");
        }

        commentairesRepository.delete(commentaire);
        log.info("Commentaire {} supprimé par {}", commentaireId, utilisateurId);
    }

    // ============================================================
    // 4. NOMBRE DE COMMENTAIRES D'UN PROJET
    // ============================================================

    @Transactional(readOnly = true)
    public long compterCommentaires(UUID projetId) {
        return commentairesRepository.countByProjetId(projetId);
    }
}
