package com.ensias.crowdfunding_project.controllers.utilisateur.admin;

import com.ensias.crowdfunding_project.dto.utilisateur.admin.AdminDashboardStats;
import com.ensias.crowdfunding_project.dto.utilisateur.admin.KycPendingResponse;
import com.ensias.crowdfunding_project.dto.utilisateur.admin.projet.ProjetAdminResponse;
import com.ensias.crowdfunding_project.services.utilisateur.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminService adminService;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<AdminDashboardStats> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/dashboard/last-pending-kyc")
    public List<KycPendingResponse> getLastPendingKyc() {
        return adminService.getLastPendingKyc(5);
    }

    @GetMapping("/dashboard/last-projects")  // ✅ corrigé
    public ResponseEntity<List<ProjetAdminResponse>> getLastProjects() {
        return ResponseEntity.ok(adminService.getLastProjects(5));
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminDashboardStats> getStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }
}