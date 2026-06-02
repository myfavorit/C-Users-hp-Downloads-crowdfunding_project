package com.ensias.crowdfunding_project.controllers.kyc;

import com.ensias.crowdfunding_project.dto.profilkyc.KycResponse;
import com.ensias.crowdfunding_project.dto.profilkyc.KycReviewRequest;
import com.ensias.crowdfunding_project.dto.profilkyc.KycSubmissionRequest;
import com.ensias.crowdfunding_project.security.util.SecurityUtils;
import com.ensias.crowdfunding_project.services.profilkyc.KycService;
import com.ensias.crowdfunding_project.services.utilisateur.FileStorageService;
import com.ensias.crowdfunding_project.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
@Slf4j
public class KycController {

    private final KycService kycService;
    private final FileStorageService fileStorageService;

    // ==================== UPLOAD PHOTO ====================
    @PostMapping("/upload")
    public ResponseEntity<String> uploadPhoto(@RequestParam("file") MultipartFile file) {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (file.isEmpty()) {
            throw new BusinessException("Le fichier est vide");
        }
        // Vérification simple du type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !"application/pdf".equals(contentType))) {
            throw new BusinessException("Seuls les images et PDF sont acceptés");
        }
        try {
            String fileUrl = fileStorageService.storeFile(file, userId);
            return ResponseEntity.ok(fileUrl);
        } catch (IOException e) {
            log.error("Erreur upload fichier", e);
            throw new BusinessException("Erreur lors du stockage du fichier");
        }
    }

    // ==================== SOUMISSION KYC ====================
    @PostMapping("/submit")
    public ResponseEntity<Void> soumettreKyc(@Valid @RequestBody KycSubmissionRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        kycService.soumettreKyc(userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<KycResponse> getMonKyc() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(kycService.getMonKyc(userId));
    }

    // ==================== ADMIN ====================
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<KycResponse>> getPendingKycs() {
        return ResponseEntity.ok(kycService.getKycsEnAttente());
    }

    @PutMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveKyc(@PathVariable UUID id) {
        UUID adminId = SecurityUtils.getCurrentUserId();
        kycService.approuverKyc(id, adminId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectKyc(@PathVariable UUID id,
                                          @Valid @RequestBody KycReviewRequest request) {
        UUID adminId = SecurityUtils.getCurrentUserId();
        kycService.rejeterKyc(id, adminId, request.getMotif());
        return ResponseEntity.ok().build();
    }
}