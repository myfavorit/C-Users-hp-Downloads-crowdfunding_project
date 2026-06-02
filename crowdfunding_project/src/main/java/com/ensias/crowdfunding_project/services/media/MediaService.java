package com.ensias.crowdfunding_project.services.media;

import com.ensias.crowdfunding_project.dto.media.MediaRequest;
import com.ensias.crowdfunding_project.dto.media.MediaResponse;
import com.ensias.crowdfunding_project.entities.Media;
import com.ensias.crowdfunding_project.entities.Projet;
import com.ensias.crowdfunding_project.entities.Utilisateur;
import com.ensias.crowdfunding_project.repositories.MediaRepository;
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
public class MediaService {

    private final MediaRepository mediaRepository;
    private final ProjetRepository projetRepository;
    private final UtilisateurRepository utilisateurRepository;

    // ============================================================
    // 1. AJOUTER UN MEDIA (PROJECT_CREATOR only)
    // ============================================================

    @Transactional
    public MediaResponse ajouterMedia(UUID projetId, MediaRequest request, UUID porteurId) {

        // 1. Verify creator exists and is active
        Utilisateur porteur = getUtilisateurActif(porteurId);

        // 2. Verify creator role
        if (!porteur.isProjectCreator()) {
            throw new RuntimeException(
                    "Seul un PROJECT_CREATOR peut ajouter des médias"
            );
        }

        // 3. Find project
        Projet projet = projetRepository.findByIdAndIsDeletedFalse(projetId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        // 4. Verify ownership
        if (!projet.getPorteur().getId().equals(porteurId)) {
            throw new RuntimeException(
                    "Vous n'êtes pas le porteur de ce projet"
            );
        }

        // 5. Validate URL
        if (request.getUrl() == null || request.getUrl().isBlank()) {
            throw new RuntimeException("L'URL est obligatoire");
        }
        if (!request.getUrl().startsWith("http://") &&
                !request.getUrl().startsWith("https://")) {
            throw new RuntimeException("L'URL doit commencer par http:// ou https://");
        }

        // 6. Build media entity
        Media media = Media.builder()
                .projet(projet)
                .url(request.getUrl().trim())
                .typeMedia(request.getTypeMedia())
                .build();

        // 7. Validate type coherence using entity method
        if (!media.isTypeCoherent()) {
            throw new RuntimeException(
                    "L'URL ne correspond pas au type de média sélectionné"
            );
        }

        Media saved = mediaRepository.save(media);
        log.info("Media ajouté: {} pour projet {}", saved.getId(), projetId);
        return MediaResponse.from(saved);
    }

    // ============================================================
    // 2. SUPPRIMER UN MEDIA (PROJECT_CREATOR only)
    // ============================================================

    @Transactional
    public void supprimerMedia(UUID mediaId, UUID porteurId) {

        // 1. Verify creator exists and is active
        getUtilisateurActif(porteurId);

        // 2. Find media
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Média introuvable"));

        // 3. Verify ownership through project
        if (!media.getProjet().getPorteur().getId().equals(porteurId)) {
            throw new RuntimeException(
                    "Vous n'êtes pas autorisé à supprimer ce média"
            );
        }

        mediaRepository.delete(media);
        log.info("Media {} supprimé par {}", mediaId, porteurId);
    }

    // ============================================================
    // 3. LISTER LES MEDIAS D'UN PROJET (public)
    // ============================================================

    @Transactional(readOnly = true)
    public List<MediaResponse> getMediasParProjet(UUID projetId) {
        return mediaRepository
                .findByProjetIdOrderByCreatedAtDesc(projetId)
                .stream()
                .map(MediaResponse::from)
                .collect(Collectors.toList());
    }

    // ============================================================
    // 4. LISTER LES MEDIAS PAR TYPE (public)
    // ============================================================

    @Transactional(readOnly = true)
    public List<MediaResponse> getMediasParType(UUID projetId, Media.TypeMedia typeMedia) {
        return mediaRepository
                .findByProjetIdAndTypeMediaOrderByCreatedAtDesc(projetId, typeMedia)
                .stream()
                .map(MediaResponse::from)
                .collect(Collectors.toList());
    }

    // ============================================================
    // 5. SUPPRIMER TOUS LES MEDIAS D'UN PROJET
    // (called internally when project is deleted)
    // ============================================================

    @Transactional
    public void supprimerTousLesMedias(UUID projetId, UUID porteurId) {

        // Verify ownership
        Projet projet = projetRepository.findByIdAndIsDeletedFalse(projetId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        if (!projet.getPorteur().getId().equals(porteurId)) {
            throw new RuntimeException("Accès refusé");
        }

        mediaRepository.deleteByProjetId(projetId);
        log.info("Tous les médias du projet {} supprimés", projetId);
    }

    // ============================================================
    // 6. COMPTER LES MEDIAS D'UN PROJET PAR TYPE
    // ============================================================

    @Transactional(readOnly = true)
    public long compterMediasParType(UUID projetId, Media.TypeMedia typeMedia) {
        return mediaRepository.countByProjetIdAndTypeMedia(projetId, typeMedia);
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