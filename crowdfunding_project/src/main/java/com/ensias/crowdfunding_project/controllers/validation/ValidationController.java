package com.ensias.crowdfunding_project.controllers.validation;

import com.ensias.crowdfunding_project.dto.validation.ValidationRequest;
import com.ensias.crowdfunding_project.dto.validation.ValidationResponse;
import com.ensias.crowdfunding_project.repositories.UtilisateurRepository;
import com.ensias.crowdfunding_project.services.validation.ValidationService;
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
@RequestMapping("/api/validations")
@RequiredArgsConstructor
public class ValidationController {

    private final ValidationService validationService;
    private final UtilisateurRepository utilisateurRepository;

    // ── POST /api/validations/valider ─────────────────────────────────────
    // Admin validates a project
    @PostMapping("/valider")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ValidationResponse> validerProjet(
            @Valid @RequestBody ValidationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID adminId = getUtilisateurId(userDetails);
        ValidationResponse response =
                validationService.validerProjet(request.getProjetId(), adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── POST /api/validations/rejeter ─────────────────────────────────────
    // Admin rejects a project
    @PostMapping("/rejeter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ValidationResponse> rejeterProjet(
            @Valid @RequestBody ValidationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID adminId = getUtilisateurId(userDetails);
        ValidationResponse response = validationService.rejeterProjet(
                request.getProjetId(),
                adminId,
                request.getMotifRefus()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── GET /api/validations/mes-decisions ────────────────────────────────
    // Admin sees all their decisions
    @GetMapping("/mes-decisions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ValidationResponse>> getDecisionsAdmin(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID adminId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(validationService.getDecisionsAdmin(adminId));
    }

    // ── GET /api/validations/projet/{projetId} ────────────────────────────
    // Get validation of a specific project (admin or creator)
    @GetMapping("/projet/{projetId}")
    public ResponseEntity<ValidationResponse> getValidationParProjet(
            @PathVariable UUID projetId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID demandeurId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(
                validationService.getValidationParProjet(projetId, demandeurId)
        );
    }

    // ── GET /api/validations/stats ────────────────────────────────────────
    // Validation statistics (admin dashboard)
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ValidationService.ValidationStats> getStats(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID adminId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(validationService.getStats(adminId));
    }

    // ── Helper ────────────────────────────────────────────────────────────
    private UUID getUtilisateurId(UserDetails userDetails) {
        return utilisateurRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"))
                .getId();
    }
}