package com.ensias.crowdfunding_project.controllers.utilisateur.admin;

import com.ensias.crowdfunding_project.dto.utilisateur.admin.projet.MotifRequest;
import com.ensias.crowdfunding_project.dto.utilisateur.admin.MotifKycRequest;

import com.ensias.crowdfunding_project.dto.profilkyc.KycResponse;
import com.ensias.crowdfunding_project.security.user.CustomUserDetails;
import com.ensias.crowdfunding_project.services.profilkyc.KycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminKycController {

    private final KycService kycService;

    @GetMapping("/kyc/en-attente")
    public List<KycResponse> getKycEnAttente() {
        return kycService.getKycsEnAttente();
    }

    @PutMapping("/kyc/{id}/approuver")
    public ResponseEntity<Void> approuverKyc(@PathVariable UUID id,
                                             @AuthenticationPrincipal CustomUserDetails admin) {
        kycService.approuverKyc(id, admin.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/kyc/{id}/rejeter")
    public ResponseEntity<Void> rejeterKyc(@PathVariable UUID id,
                                           @Valid @RequestBody MotifRequest request,
                                           @AuthenticationPrincipal CustomUserDetails admin) {
        kycService.rejeterKyc(id, admin.getId(), request.getMotif());
        return ResponseEntity.ok().build();
    }
}