package com.ensias.crowdfunding_project.controllers.media;

import com.ensias.crowdfunding_project.dto.media.MediaRequest;
import com.ensias.crowdfunding_project.dto.media.MediaResponse;
import com.ensias.crowdfunding_project.entities.Media;
import com.ensias.crowdfunding_project.repositories.UtilisateurRepository;
import com.ensias.crowdfunding_project.services.media.MediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;
    private final UtilisateurRepository utilisateurRepository;

    // ── POST /api/media/projet/{projetId} ─────────────────────────────────
    // Add media to a project (PROJECT_CREATOR only)
    @PostMapping("/projet/{projetId}")
    @PreAuthorize("hasRole('PROJECT_CREATOR')")
    public ResponseEntity<MediaResponse> ajouterMedia(
            @PathVariable UUID projetId,
            @Valid @RequestBody MediaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID porteurId = getUtilisateurId(userDetails);
        MediaResponse response = mediaService.ajouterMedia(projetId, request, porteurId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── DELETE /api/media/{mediaId} ───────────────────────────────────────
    // Delete a media (PROJECT_CREATOR only)
    @DeleteMapping("/{mediaId}")
    @PreAuthorize("hasRole('PROJECT_CREATOR')")
    public ResponseEntity<Void> supprimerMedia(
            @PathVariable UUID mediaId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID porteurId = getUtilisateurId(userDetails);
        mediaService.supprimerMedia(mediaId, porteurId);
        return ResponseEntity.noContent().build();
    }

    // ── GET /api/media/projet/{projetId} ──────────────────────────────────
    // Get all media of a project (public)
    @GetMapping("/projet/{projetId}")
    public ResponseEntity<List<MediaResponse>> getMediasParProjet(
            @PathVariable UUID projetId) {
        return ResponseEntity.ok(mediaService.getMediasParProjet(projetId));
    }

    // ── GET /api/media/projet/{projetId}/type/{typeMedia} ─────────────────
    // Get media of a project filtered by type (public)
    @GetMapping("/projet/{projetId}/type/{typeMedia}")
    public ResponseEntity<List<MediaResponse>> getMediasParType(
            @PathVariable UUID projetId,
            @PathVariable Media.TypeMedia typeMedia) {
        return ResponseEntity.ok(
                mediaService.getMediasParType(projetId, typeMedia)
        );
    }

    // ── DELETE /api/media/projet/{projetId}/tous ──────────────────────────
    // Delete all media of a project (PROJECT_CREATOR only)
    @DeleteMapping("/projet/{projetId}/tous")
    @PreAuthorize("hasRole('PROJECT_CREATOR')")
    public ResponseEntity<Void> supprimerTousLesMedias(
            @PathVariable UUID projetId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID porteurId = getUtilisateurId(userDetails);
        mediaService.supprimerTousLesMedias(projetId, porteurId);
        return ResponseEntity.noContent().build();
    }

    // ── GET /api/media/projet/{projetId}/compter/{typeMedia} ──────────────
    // Count media by type (public)
    @GetMapping("/projet/{projetId}/compter/{typeMedia}")
    public ResponseEntity<Long> compterMediasParType(
            @PathVariable UUID projetId,
            @PathVariable Media.TypeMedia typeMedia) {
        return ResponseEntity.ok(
                mediaService.compterMediasParType(projetId, typeMedia)
        );
    }

    // ── Helper ────────────────────────────────────────────────────────────
    private UUID getUtilisateurId(UserDetails userDetails) {
        return utilisateurRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"))
                .getId();
    }
}