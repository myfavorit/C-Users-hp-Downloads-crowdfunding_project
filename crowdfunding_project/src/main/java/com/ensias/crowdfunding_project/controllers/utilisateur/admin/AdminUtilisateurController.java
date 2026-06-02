package com.ensias.crowdfunding_project.controllers.utilisateur.admin;

import com.ensias.crowdfunding_project.dto.utilisateur.admin.ModifierStatutRequest;
import com.ensias.crowdfunding_project.dto.utilisateur.admin.UserResponse;
import com.ensias.crowdfunding_project.security.user.CustomUserDetails;
import com.ensias.crowdfunding_project.services.utilisateur.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUtilisateurController {

    private final AdminService adminService;

    @GetMapping("/utilisateurs")
    public Page<UserResponse> getAllUtilisateurs(Pageable pageable,
                                                 @RequestParam(required = false) String role,
                                                 @RequestParam(required = false) String statut) {
        // Le service retourne déjà Page<UserResponse> – pas besoin de map
        return adminService.getAllUtilisateurs(pageable, role, statut);
    }

    @GetMapping("/utilisateurs/{id}")
    public UserResponse getUtilisateurDetail(@PathVariable UUID id) {
        return adminService.getUtilisateurDetail(id);
    }

    @PutMapping("/utilisateurs/{id}/statut")
    public ResponseEntity<Void> modifierStatutUtilisateur(@PathVariable UUID id,
                                                          @Valid @RequestBody ModifierStatutRequest request,
                                                          @AuthenticationPrincipal CustomUserDetails admin) {
        adminService.modifierStatutUtilisateur(id, request.getStatut(), admin.getId(), request.getMotif());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/utilisateurs/{id}")
    public ResponseEntity<Void> supprimerUtilisateur(@PathVariable UUID id,
                                                     @AuthenticationPrincipal CustomUserDetails admin) {
        adminService.supprimerUtilisateurPhysiquement(id, admin.getId());
        return ResponseEntity.ok().build();
    }
}