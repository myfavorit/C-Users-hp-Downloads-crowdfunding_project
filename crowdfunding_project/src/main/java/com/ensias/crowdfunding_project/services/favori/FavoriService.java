package com.ensias.crowdfunding_project.services.favori;

import com.ensias.crowdfunding_project.dto.favori.FavoriResponse;
import com.ensias.crowdfunding_project.entities.Favori;
import com.ensias.crowdfunding_project.entities.Projet;
import com.ensias.crowdfunding_project.entities.Utilisateur;
import com.ensias.crowdfunding_project.repositories.FavoriRepository;
import com.ensias.crowdfunding_project.repositories.ProjetRepository;
import com.ensias.crowdfunding_project.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriService {

    private final FavoriRepository favoriRepository;
    private final ProjetRepository projetRepository;
    private final UtilisateurRepository utilisateurRepository;

    // ============================================================
    // 1. AJOUTER UN FAVORI
    // ============================================================

    @Transactional
    public FavoriResponse ajouterFavori(UUID projetId, UUID utilisateurId) {

        // 1. Verify user exists and is active
        Utilisateur utilisateur = getUtilisateurActif(utilisateurId);

        // 2. Find project
        Projet projet = projetRepository.findByIdAndIsDeletedFalse(projetId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        // 3. Check not already in favorites
        if (favoriRepository.existsByUtilisateurIdAndProjetId(utilisateurId, projetId)) {
            throw new RuntimeException("Ce projet est déjà dans vos favoris");
        }

        // 4. Cannot favorite your own project
        if (projet.getPorteur().getId().equals(utilisateurId)) {
            throw new RuntimeException(
                    "Vous ne pouvez pas ajouter votre propre projet en favori"
            );
        }

        // 5. Build and save
        Favori favori = Favori.builder()
                .utilisateur(utilisateur)
                .projet(projet)
                .build();

        Favori saved = favoriRepository.save(favori);
        log.info("Favori ajouté: utilisateur {} → projet {}", utilisateurId, projetId);
        return FavoriResponse.from(saved);
    }

    // ============================================================
    // 2. SUPPRIMER UN FAVORI
    // ============================================================

    @Transactional
    public void supprimerFavori(UUID projetId, UUID utilisateurId) {

        // Verify user exists
        getUtilisateurActif(utilisateurId);

        // Find the specific favori
        Favori favori = favoriRepository
                .findByUtilisateurIdAndProjetId(utilisateurId, projetId)
                .orElseThrow(() -> new RuntimeException(
                        "Ce projet n'est pas dans vos favoris"
                ));

        // Verify ownership
        if (!favori.estProprietaire(utilisateurId)) {
            throw new RuntimeException("Accès refusé");
        }

        favoriRepository.delete(favori);
        log.info("Favori supprimé: utilisateur {} → projet {}", utilisateurId, projetId);
    }

    // ============================================================
    // 3. MES FAVORIS (with project details)
    // ============================================================

    @Transactional(readOnly = true)
    public List<FavoriResponse> getMesFavoris(UUID utilisateurId) {

        getUtilisateurActif(utilisateurId);

        // Uses JOIN FETCH to avoid N+1 problem
        return favoriRepository
                .findByUtilisateurIdWithProjet(utilisateurId)
                .stream()
                .map(FavoriResponse::from)
                .collect(Collectors.toList());
    }

    // ============================================================
    // 4. VÉRIFIER SI UN PROJET EST EN FAVORI
    // ============================================================

    @Transactional(readOnly = true)
    public boolean estFavori(UUID projetId, UUID utilisateurId) {
        return favoriRepository.existsByUtilisateurIdAndProjetId(utilisateurId, projetId);
    }

    // ============================================================
    // 5. NOMBRE DE FAVORIS D'UN PROJET
    // ============================================================

    @Transactional(readOnly = true)
    public long getNombreFavoris(UUID projetId) {
        return favoriRepository.countByProjetId(projetId);
    }

    // ============================================================
    // PRIVATE HELPER
    // ============================================================

    private Utilisateur getUtilisateurActif(UUID utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        if (!utilisateur.isActif()) {
            throw new RuntimeException("Compte suspendu ou banni");
        }
        return utilisateur;
    }
}