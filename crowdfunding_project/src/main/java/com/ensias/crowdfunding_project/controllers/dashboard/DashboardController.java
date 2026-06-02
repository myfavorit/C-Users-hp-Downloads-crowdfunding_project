package com.ensias.crowdfunding_project.controllers.dashboard;

import com.ensias.crowdfunding_project.dto.dashboard.AdminDashboardResponse;
import com.ensias.crowdfunding_project.dto.dashboard.CreatorDashboardResponse;
import com.ensias.crowdfunding_project.dto.dashboard.InvestorDashboardResponse;
import com.ensias.crowdfunding_project.repositories.UtilisateurRepository;
import com.ensias.crowdfunding_project.services.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UtilisateurRepository utilisateurRepository;

    // ── GET /api/dashboard/admin ──────────────────────────────────────────
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResponse> getAdminDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID adminId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(dashboardService.getAdminDashboard(adminId));
    }

    // ── GET /api/dashboard/investor ───────────────────────────────────────
    @GetMapping("/investor")
    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<InvestorDashboardResponse> getInvestorDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID investisseurId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(dashboardService.getInvestorDashboard(investisseurId));
    }

    // ── GET /api/dashboard/creator ────────────────────────────────────────
    @GetMapping("/creator")
    @PreAuthorize("hasRole('PROJECT_CREATOR')")
    public ResponseEntity<CreatorDashboardResponse> getCreatorDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID porteurId = getUtilisateurId(userDetails);
        return ResponseEntity.ok(dashboardService.getCreatorDashboard(porteurId));
    }

    // ── Helper ────────────────────────────────────────────────────────────
    private UUID getUtilisateurId(UserDetails userDetails) {
        return utilisateurRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"))
                .getId();
    }
}