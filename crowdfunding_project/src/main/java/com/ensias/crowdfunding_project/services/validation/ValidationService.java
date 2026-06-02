package com.ensias.crowdfunding_project.services.validation;

import com.ensias.crowdfunding_project.dto.validation.ValidationRequest;
import com.ensias.crowdfunding_project.dto.validation.ValidationResponse;
import com.ensias.crowdfunding_project.entities.*;
import com.ensias.crowdfunding_project.enums.StatutProjet;
import com.ensias.crowdfunding_project.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationService {

    private final ValidationRepository validationRepository;
    private final ProjetRepository projetRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationRepository notificationRepository;

    // ============================================================
    // 1. VALIDER UN PROJET (ADMIN only — verified by ID in DB)
    // ============================================================

    @Transactional
    public ValidationResponse validerProjet(UUID projetId, UUID adminId) {

        // 1. Double check admin in DB
        Utilisateur admin = getAdminVerifie(adminId);

        // 2. Find project
        Projet projet = projetRepository.findByIdAndIsDeletedFalse(projetId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        // 3. Check project status
        if (projet.getStatut() != StatutProjet.EN_ATTENTE) {
            throw new RuntimeException(
                    "Seul un projet EN_ATTENTE peut être validé — statut actuel: "
                            + projet.getStatut()
            );
        }

        // 4. Check no validation already exists
        if (validationRepository.existsByProjetId(projetId)) {
            throw new RuntimeException(
                    "Ce projet a déjà une décision de validation"
            );
        }

        // 5. Create validation record using entity factory method
        Validation validation = Validation.valider(projet, admin);
        validationRepository.save(validation);

        // 6. Update project status using entity business method
        projet.valider();
        projetRepository.save(projet);

        // 7. Notify project creator
        Notification notification = Notification.projetValide(
                projet.getPorteur(),
                projet.getTitre()
        );
        notificationRepository.save(notification);

        log.info("Projet {} validé par admin {}", projetId, adminId);
        return ValidationResponse.from(validation);
    }

    // ============================================================
    // 2. REJETER UN PROJET (ADMIN only — verified by ID in DB)
    // ============================================================

    @Transactional
    public ValidationResponse rejeterProjet(
            UUID projetId, UUID adminId, String motifRefus) {

        // 1. Double check admin in DB
        Utilisateur admin = getAdminVerifie(adminId);

        // 2. Motif is mandatory for rejection
        if (motifRefus == null || motifRefus.isBlank()) {
            throw new RuntimeException("Le motif de refus est obligatoire");
        }

        // 3. Find project
        Projet projet = projetRepository.findByIdAndIsDeletedFalse(projetId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        // 4. Check project status
        if (projet.getStatut() != StatutProjet.EN_ATTENTE) {
            throw new RuntimeException(
                    "Seul un projet EN_ATTENTE peut être rejeté — statut actuel: "
                            + projet.getStatut()
            );
        }

        // 5. Check no validation already exists
        if (validationRepository.existsByProjetId(projetId)) {
            throw new RuntimeException(
                    "Ce projet a déjà une décision de validation"
            );
        }

        // 6. Create rejection record using entity factory method
        Validation validation = Validation.rejeter(projet, admin, motifRefus);
        validationRepository.save(validation);

        // 7. Update project status using entity business method
        projet.refuser();
        projetRepository.save(projet);

        // 8. Notify project creator with reason
        Notification notification = Notification.projetRejete(
                projet.getPorteur(),
                projet.getTitre(),
                motifRefus
        );
        notificationRepository.save(notification);

        log.info("Projet {} rejeté par admin {} — motif: {}",
                projetId, adminId, motifRefus);
        return ValidationResponse.from(validation);
    }

    // ============================================================
    // 3. HISTORIQUE DES DÉCISIONS D'UN ADMIN
    // ============================================================

    @Transactional(readOnly = true)
    public List<ValidationResponse> getDecisionsAdmin(UUID adminId) {

        getAdminVerifie(adminId);

        return validationRepository
                .findByAdminIdWithProjet(adminId)
                .stream()
                .map(ValidationResponse::from)
                .collect(Collectors.toList());
    }

    // ============================================================
    // 4. VALIDATION D'UN PROJET SPÉCIFIQUE
    // ============================================================

    @Transactional(readOnly = true)
    public ValidationResponse getValidationParProjet(UUID projetId, UUID demandeurId) {

        // Verify demandeur exists
        Utilisateur demandeur = utilisateurRepository.findById(demandeurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Find project
        Projet projet = projetRepository.findByIdAndIsDeletedFalse(projetId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        // Only admin or the project creator can see the validation
        boolean isAdmin   = demandeur.isAdmin();
        boolean isCreator = projet.getPorteur().getId().equals(demandeurId);

        if (!isAdmin && !isCreator) {
            throw new RuntimeException("Accès refusé");
        }

        Validation validation = validationRepository
                .findByProjetIdWithDetails(projetId)
                .orElseThrow(() -> new RuntimeException(
                        "Aucune décision de validation pour ce projet"
                ));

        return ValidationResponse.from(validation);
    }

    // ============================================================
    // 5. STATISTIQUES (admin dashboard)
    // ============================================================

    @Transactional(readOnly = true)
    public ValidationStats getStats(UUID adminId) {

        getAdminVerifie(adminId);

        long totalValides  = validationRepository.countByDecision(Validation.Decision.VALIDE);
        long totalRejetes  = validationRepository.countByDecision(Validation.Decision.REJETE);
        long totalEnAttente = projetRepository
                .findByStatutAndIsDeletedFalse(StatutProjet.EN_ATTENTE).size();
        Double tauxApprobation = validationRepository.getTauxApprobation();

        return new ValidationStats(
                totalValides,
                totalRejetes,
                totalEnAttente,
                tauxApprobation != null ? tauxApprobation : 0.0
        );
    }

    // ============================================================
    // PRIVATE HELPERS
    // ============================================================

    /**
     * DB double-check for admin
     * JWT says ADMIN + DB confirms role=ADMIN AND statut=ACTIF
     */
    private Utilisateur getAdminVerifie(UUID adminId) {
        Utilisateur admin = utilisateurRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin introuvable"));

        if (!admin.isAdmin()) {
            log.warn("Tentative d'accès admin refusée pour userId: {}", adminId);
            throw new RuntimeException(
                    "Accès refusé — droits administrateur requis"
            );
        }

        if (!admin.isActif()) {
            log.warn("Compte admin inactif: {}", adminId);
            throw new RuntimeException(
                    "Compte administrateur suspendu ou banni"
            );
        }

        return admin;
    }

    // ============================================================
    // INNER CLASS — Stats DTO
    // ============================================================

    public record ValidationStats(
            long totalValides,
            long totalRejetes,
            long totalEnAttente,
            double tauxApprobation
    ) {}
}