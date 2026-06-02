package com.ensias.crowdfunding_project.controllers.favori;

import com.ensias.crowdfunding_project.dto.favori.FavoriResponse;
import com.ensias.crowdfunding_project.repositories.UtilisateurRepository;
import com.ensias.crowdfunding_project.services.favori.FavoriService;
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
@RequestMapping("/api/favoris")
@RequiredArgsConstructor
public class FavoriController {

    private final FavoriService favoriService;
    private final UtilisateurRepository utilisateurRepository;

    // ── POST /api/favoris/{projetId} ──────────────────────────────────────
    // Add project to favorites
    @PostMapping("/{projetId}")
    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<FavoriResponse> ajouterFavori(
            @PathVariable UUID projetId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID utilisateurId = getUtilisateurId(userDetails);
        FavoriResponse response = favoriService.ajouterFavori(projetId, utilisateurId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── DELETE /api/favoris/{projetId} ────────────────────────────────────
    // Remove project from favorites
    @DeleteMapping("/{projetId}")
    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<Void> supprimerFavori(
            @PathVariable UUID projetId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID utilisateurId = getUtilisateurId(userDetails);
        favoriService.supprimerFavori(projetId, utilisateurId);
        return ResponseEntity.noContent().build();
    }

    // ── GET /api/favoris/mes-favoris ──────────────────────────────────────
    // Get my favorites list
    @GetMapping("/mes-favoris")
    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<List<FavoriResponse>> getMesFavoris(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID utilisateurId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(favoriService.getMesFavoris(utilisateurId));
    }

    // ── GET /api/favoris/{projetId}/est-favori ────────────────────────────
    // Check if a project is in favorites (for heart icon on frontend)
    @GetMapping("/{projetId}/est-favori")
    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<Boolean> estFavori(
            @PathVariable UUID projetId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID utilisateurId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(favoriService.estFavori(projetId, utilisateurId));
    }

    // ── GET /api/favoris/{projetId}/nombre ───────────────────────────────
    // Get number of favorites for a project (public)
    @GetMapping("/{projetId}/nombre")
    public ResponseEntity<Long> getNombreFavoris(@PathVariable UUID projetId) {
        return ResponseEntity.ok(favoriService.getNombreFavoris(projetId));
    }

    // ── Helper ────────────────────────────────────────────────────────────
    private UUID getUtilisateurId(UserDetails userDetails) {
        return utilisateurRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"))
                .getId();
    }
}