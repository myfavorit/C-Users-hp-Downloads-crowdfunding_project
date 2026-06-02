package com.ensias.crowdfunding_project.services.profilkyc;

import com.ensias.crowdfunding_project.dto.profilkyc.KycResponse;
import com.ensias.crowdfunding_project.dto.profilkyc.KycSubmissionRequest;
import com.ensias.crowdfunding_project.entities.ProfilKyc;
import com.ensias.crowdfunding_project.entities.Utilisateur;
import com.ensias.crowdfunding_project.enums.RoleUtilisateur;
import com.ensias.crowdfunding_project.enums.TypeNotification;
import com.ensias.crowdfunding_project.exception.BusinessException;
import com.ensias.crowdfunding_project.exception.ResourceNotFoundException;
import com.ensias.crowdfunding_project.repositories.ProfilKycRepository;
import com.ensias.crowdfunding_project.repositories.UtilisateurRepository;
import com.ensias.crowdfunding_project.services.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KycService {

    private final ProfilKycRepository profilKycRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;

    // ========== UTILISATEUR ==========

    public void soumettreKyc(UUID userId, KycSubmissionRequest request) {
        if (request.getRib() == null || request.getRib().isBlank()) {
            throw new BusinessException("Le RIB est obligatoire.");
        }
        if (!request.getRib().matches("^[A-Za-z0-9]{23}$")) {
            throw new BusinessException("Le format du RIB est invalide (23 caractères alphanumériques).");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        ProfilKyc profil = profilKycRepository.findByUtilisateurId(userId)
                .orElseGet(() -> ProfilKyc.builder().utilisateur(utilisateur).build());

        if (profil.isKycValide()) {
            throw new BusinessException("Votre KYC est déjà validé. Vous ne pouvez pas le modifier.");
        }

        // Mise à jour des champs
        if (request.getPhotoProfil() != null) profil.setPhotoProfil(request.getPhotoProfil());
        if (request.getBio() != null) profil.setBio(request.getBio());
        profil.setRib(request.getRib());
        profil.setKycSoumisAt(LocalDateTime.now());
        profil.setKycValide(false);
        profil.setKycValideAt(null);
        // Nettoyage des traces de rejet
        profil.setMotifRejet(null);
        profil.setDateTraitement(null);

        profilKycRepository.save(profil);
        log.info("KYC soumis par l'utilisateur {}", userId);

        notifierAdminsNouveauKyc(profil);
    }

    @Transactional(readOnly = true)
    public KycResponse getMonKyc(UUID userId) {
        return profilKycRepository.findByUtilisateurId(userId)
                .map(this::toResponse)
                .orElse(null);
    }

    // ========== ADMIN ==========

    @Transactional(readOnly = true)
    public List<KycResponse> getKycsEnAttente() {
        return profilKycRepository.findPendingKycWithUtilisateur()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void approuverKyc(UUID kycId, UUID adminId) {
        ProfilKyc profil = profilKycRepository.findById(kycId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC non trouvé"));

        if (!profil.estEnAttente()) {
            throw new BusinessException("Seule une demande KYC en attente peut être approuvée.");
        }

        utilisateurRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin non trouvé"));

        profil.valider();
        profilKycRepository.save(profil);

        notificationService.envoyerNotification(
                profil.getUtilisateur().getId(),
                "KYC validé",
                "Votre dossier KYC a été approuvé. Vous pouvez maintenant investir et créer des projets.",
                TypeNotification.KYC_VALIDE
        );
        log.info("KYC {} approuvé par admin {}", kycId, adminId);
    }

    public void rejeterKyc(UUID kycId, UUID adminId, String motif) {
        if (motif == null || motif.trim().isEmpty()) {
            throw new BusinessException("Le motif de rejet est obligatoire.");
        }

        ProfilKyc profil = profilKycRepository.findById(kycId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC non trouvé"));

        if (!profil.estEnAttente()) {
            throw new BusinessException("Seule une demande KYC en attente peut être rejetée.");
        }

        utilisateurRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin non trouvé"));

        profil.rejeter();
        profil.setMotifRejet(motif);
        profil.setDateTraitement(LocalDateTime.now());
        profilKycRepository.save(profil);

        String message = "Votre dossier KYC a été refusé. Motif : " + motif;
        notificationService.envoyerNotification(
                profil.getUtilisateur().getId(),
                "KYC refusé",
                message,
                TypeNotification.KYC_REJETE
        );
        log.info("KYC {} rejeté par admin {} : {}", kycId, adminId, motif);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void supprimerKyc(UUID kycId) {
        ProfilKyc profil = profilKycRepository.findById(kycId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC non trouvé"));
        profilKycRepository.delete(profil);
        log.info("KYC {} supprimé par admin", kycId);
    }

    // ========== HELPERS ==========

    private void notifierAdminsNouveauKyc(ProfilKyc profil) {
        List<Utilisateur> admins = utilisateurRepository.findByRole(RoleUtilisateur.ADMIN);
        for (Utilisateur admin : admins) {
            notificationService.envoyerNotification(
                    admin.getId(),
                    "Nouveau dossier KYC en attente",
                    String.format("L'utilisateur %s %s a soumis un dossier KYC à vérifier.",
                            profil.getUtilisateur().getPrenom(),
                            profil.getUtilisateur().getNom()),
                    TypeNotification.KYC_SOUMIS
            );
        }
    }

    private KycResponse toResponse(ProfilKyc profil) {
        String statut = profil.estEnAttente() ? "EN_ATTENTE" : (profil.isKycValide() ? "APPROUVE" : "REJETE");
        return KycResponse.builder()
                .id(profil.getId())
                .utilisateurId(profil.getUtilisateur().getId())
                .nomUtilisateur(profil.getUtilisateur().getNom())   // ajouté
                .photoProfil(profil.getPhotoProfil())
                .bio(profil.getBio())
                .rib(profil.getRib())
                .kycValide(profil.isKycValide())
                .kycSoumisAt(profil.getKycSoumisAt())
                .kycValideAt(profil.getKycValideAt())
                .statut(statut)   // facultatif, si votre DTO l’accepte
                .build();
    }
}