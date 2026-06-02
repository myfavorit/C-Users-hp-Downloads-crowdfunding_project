package com.ensias.crowdfunding_project.controllers.investissement;

import com.ensias.crowdfunding_project.dto.investissement.InvestissementRequest;
import com.ensias.crowdfunding_project.dto.investissement.InvestissementResponse;
import com.ensias.crowdfunding_project.repositories.UtilisateurRepository;
import com.ensias.crowdfunding_project.services.investissement.InvestissementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/investissements")
@RequiredArgsConstructor
public class InvestissementController {

    private final InvestissementService investissementService;
    private final UtilisateurRepository utilisateurRepository;

    // ── POST /api/investissements ─────────────────────────────────────────
    // Invest in a project (INVESTOR only)
    @PostMapping
    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<InvestissementResponse> investir(
            @Valid @RequestBody InvestissementRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID investisseurId = getUtilisateurId(userDetails);
        InvestissementResponse response =
                investissementService.investir(request, investisseurId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── GET /api/investissements/mes-investissements ───────────────────────
    // My investments (INVESTOR dashboard)
    @GetMapping("/mes-investissements")
    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<List<InvestissementResponse>> getMesInvestissements(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID investisseurId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(
                investissementService.getMesInvestissements(investisseurId)
        );
    }

    // ── GET /api/investissements/{id} ─────────────────────────────────────
    // Investment detail (investor himself or admin)
    @GetMapping("/{id}")
    public ResponseEntity<InvestissementResponse> getInvestissementById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID demandeurId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(
                investissementService.getInvestissementById(id, demandeurId)
        );
    }

    // ── GET /api/investissements/projet/{projetId} ────────────────────────
    // All investments of a project (creator or admin)
    @GetMapping("/projet/{projetId}")
    public ResponseEntity<List<InvestissementResponse>> getInvestissementsParProjet(
            @PathVariable UUID projetId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID demandeurId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(
                investissementService.getInvestissementsParProjet(projetId, demandeurId)
        );
    }

    // ── GET /api/investissements/mes-investissements/total ────────────────
    // Total amount invested by investor (dashboard stat)
    @GetMapping("/mes-investissements/total")
    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<BigDecimal> getMontantTotalInvesti(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID investisseurId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(
                investissementService.getMontantTotalInvesti(investisseurId)
        );
    }

    // ── GET /api/investissements/projet/{projetId}/stats ──────────────────
    // Project investment stats (admin only)
    @GetMapping("/projet/{projetId}/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InvestissementService.ProjetInvestissementStats> getStatsProjet(
            @PathVariable UUID projetId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID adminId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(
                investissementService.getStatsProjet(projetId, adminId)
        );
    }

    // ── Helper ────────────────────────────────────────────────────────────
    private UUID getUtilisateurId(UserDetails userDetails) {
        return utilisateurRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"))
                .getId();
    }
}