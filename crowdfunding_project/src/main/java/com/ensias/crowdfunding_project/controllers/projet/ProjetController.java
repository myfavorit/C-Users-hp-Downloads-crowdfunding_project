package com.ensias.crowdfunding_project.controllers.projet;

import com.ensias.crowdfunding_project.dto.projet.ProjetRequest;
import com.ensias.crowdfunding_project.dto.projet.ProjetResponse;
import com.ensias.crowdfunding_project.enums.DomaineProjet;
import com.ensias.crowdfunding_project.enums.StatutProjet;
import com.ensias.crowdfunding_project.repositories.UtilisateurRepository;
import com.ensias.crowdfunding_project.services.projet.ProjetService;
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
@RequestMapping("/api/projets")
@RequiredArgsConstructor
public class ProjetController {

    private final ProjetService projetService;
    private final UtilisateurRepository utilisateurRepository;

    // ── POST /api/projets ─────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('PROJECT_CREATOR')")
    public ResponseEntity<ProjetResponse> creerProjet(
            @Valid @RequestBody ProjetRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID porteurId = getUtilisateurId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projetService.creerProjet(request, porteurId));
    }

    // ── PUT /api/projets/{id} ─────────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROJECT_CREATOR')")
    public ResponseEntity<ProjetResponse> modifierProjet(
            @PathVariable UUID id,
            @Valid @RequestBody ProjetRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID porteurId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(projetService.modifierProjet(id, request, porteurId));
    }

    // ── POST /api/projets/{id}/soumettre ──────────────────────────────────
    @PostMapping("/{id}/soumettre")
    @PreAuthorize("hasRole('PROJECT_CREATOR')")
    public ResponseEntity<ProjetResponse> soumettreProjet(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID porteurId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(projetService.soumettreProjet(id, porteurId));
    }

    // ── POST /api/projets/{id}/annuler ────────────────────────────────────
    @PostMapping("/{id}/annuler")
    @PreAuthorize("hasRole('PROJECT_CREATOR')")
    public ResponseEntity<ProjetResponse> annulerProjet(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID porteurId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(projetService.annulerProjet(id, porteurId));
    }

    // ── DELETE /api/projets/{id} ──────────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROJECT_CREATOR')")
    public ResponseEntity<Void> supprimerProjet(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID porteurId = getUtilisateurId(userDetails);
        projetService.supprimerProjet(id, porteurId);
        return ResponseEntity.noContent().build();
    }

    // ── POST /api/projets/{id}/valider ────────────────────────────────────
    @PostMapping("/{id}/valider")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjetResponse> validerProjet(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID adminId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(projetService.validerProjet(id, adminId));
    }

    // ── POST /api/projets/{id}/rejeter ────────────────────────────────────
    @PostMapping("/{id}/rejeter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjetResponse> rejeterProjet(
            @PathVariable UUID id,
            @RequestParam String motifRefus,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID adminId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(projetService.rejeterProjet(id, adminId, motifRefus));
    }

    // ── POST /api/projets/{id}/cloturer ───────────────────────────────────
    @PostMapping("/{id}/cloturer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjetResponse> cloturerProjet(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID adminId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(projetService.cloturerProjet(id, adminId));
    }

    // ── GET /api/projets ──────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<ProjetResponse>> getProjetsActifs() {
        return ResponseEntity.ok(projetService.getProjetsActifs());
    }

    // ── GET /api/projets/search?motCle=xxx ────────────────────────────────
    @GetMapping("/search")
    public ResponseEntity<List<ProjetResponse>> rechercherParTitre(
            @RequestParam String motCle) {
        return ResponseEntity.ok(projetService.rechercherParTitre(motCle));
    }

    // ── GET /api/projets/{id} ─────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ProjetResponse> getProjetById(@PathVariable UUID id) {
        return ResponseEntity.ok(projetService.getProjetById(id));
    }

    // ── GET /api/projets/mes-projets ──────────────────────────────────────
    @GetMapping("/mes-projets")
    @PreAuthorize("hasRole('PROJECT_CREATOR')")
    public ResponseEntity<List<ProjetResponse>> getMesProjets(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID porteurId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(projetService.getMesProjets(porteurId));
    }

    // ── GET /api/projets/mes-projets/statut/{statut} ──────────────────────
    @GetMapping("/mes-projets/statut/{statut}")
    @PreAuthorize("hasRole('PROJECT_CREATOR')")
    public ResponseEntity<List<ProjetResponse>> getMesProjetsParStatut(
            @PathVariable StatutProjet statut,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID porteurId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(projetService.getMesProjetsParStatut(porteurId, statut));
    }

    // ── GET /api/projets/en-attente ───────────────────────────────────────
    @GetMapping("/en-attente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProjetResponse>> getProjetsEnAttente(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID adminId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(projetService.getProjetsEnAttente(adminId));
    }

    // ── GET /api/projets/domaine/{domaine} ────────────────────────────────
    @GetMapping("/domaine/{domaine}")
    public ResponseEntity<List<ProjetResponse>> getProjetsByDomaine(
            @PathVariable DomaineProjet domaine) {
        return ResponseEntity.ok(projetService.getProjetsByDomaine(domaine));
    }

    // ── Helper ────────────────────────────────────────────────────────────
    private UUID getUtilisateurId(UserDetails userDetails) {
        return utilisateurRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"))
                .getId();
    }
}