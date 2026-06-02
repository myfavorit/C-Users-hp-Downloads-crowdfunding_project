package com.ensias.crowdfunding_project.services.utilisateur;

import com.ensias.crowdfunding_project.dto.utilisateur.user.UserProfileResponse;
import com.ensias.crowdfunding_project.dto.utilisateur.user.UserProfileUpdateRequest;
import com.ensias.crowdfunding_project.entities.Utilisateur;
import com.ensias.crowdfunding_project.enums.StatutCompte;
import com.ensias.crowdfunding_project.enums.StatutPaiement;
import com.ensias.crowdfunding_project.enums.StatutProjet;
import com.ensias.crowdfunding_project.exception.BusinessException;
import com.ensias.crowdfunding_project.exception.ResourceNotFoundException;
import com.ensias.crowdfunding_project.repositories.InvestissementRepository;
import com.ensias.crowdfunding_project.repositories.ProjetRepository;
import com.ensias.crowdfunding_project.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserProfileService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProjetRepository projetRepository;
    private final InvestissementRepository investissementRepository;
    private final PasswordEncoder passwordEncoder;

    // ========================= CONSULTATION =========================

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        log.info("Récupération profil pour userId: {}", userId);
        Utilisateur user = getUserOrThrow(userId);
        return toProfileResponse(user);
    }

    // ========================= MODIFICATION =========================

    @Transactional
    public UserProfileResponse updateProfile(
            UUID userId,
            UserProfileUpdateRequest request) {

        log.info("Mise à jour profil pour userId: {}", userId);
        Utilisateur user = getUserOrThrow(userId);

        // Nom et prénom
        if (request.getNom() != null)
            user.setNom(request.getNom());
        if (request.getPrenom() != null)
            user.setPrenom(request.getPrenom());

        // ✅ Changement email avec revalidation
        if (request.getEmail() != null
                && !request.getEmail().equals(user.getEmail())) {
            if (utilisateurRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Email déjà utilisé");
            }
            user.setEmail(request.getEmail());
            user.setStatut(StatutCompte.INACTIF); // revalidation requise
            log.info("Email modifié — compte remis en INACTIF pour userId: {}", userId);
        }

        // ✅ Changement mot de passe
        if (request.getAncienMotDePasse() != null
                && request.getNouveauMotDePasse() != null) {
            if (!passwordEncoder.matches(
                    request.getAncienMotDePasse(),
                    user.getMotDePasseHash())) {
                throw new BusinessException("Ancien mot de passe incorrect");
            }
            user.setMotDePasseHash(
                    passwordEncoder.encode(request.getNouveauMotDePasse()));
            log.info("Mot de passe modifié pour userId: {}", userId);
        }

        // ✅ Mise à jour du timestamp
        user.setUpdatedAt(LocalDateTime.now());
        utilisateurRepository.save(user);

        return toProfileResponse(user);
    }

    // ========================= SUPPRESSION =========================

    @Transactional
    public void deleteAccount(UUID userId) {
        log.info("Demande suppression compte pour userId: {}", userId);
        Utilisateur user = getUserOrThrow(userId);

        // ✅ Switch enum direct
        switch (user.getRole()) {
            case INVESTOR -> deleteInvestorAccount(user);
            case PROJECT_CREATOR -> deleteCreatorAccount(user);
            default -> throw new BusinessException(
                    "Rôle non supporté pour cette opération");
        }
    }

    // ========================= MÉTHODES PRIVÉES =========================

    private void deleteInvestorAccount(Utilisateur user) {
        boolean hasActiveInvestments = investissementRepository
                .existsByInvestisseurIdAndStatutPaiement(
                        user.getId(), StatutPaiement.CONFIRME);

        if (hasActiveInvestments) {
            anonymizeUser(user);
            user.setStatut(StatutCompte.ANNULE);
            user.setDeleted(true);
            user.setUpdatedAt(LocalDateTime.now());
            utilisateurRepository.save(user);
            log.info("Compte INVESTOR anonymisé — userId: {}", user.getId());
        } else {
            utilisateurRepository.delete(user);
            log.info("Compte INVESTOR supprimé — userId: {}", user.getId());
        }
    }

    private void deleteCreatorAccount(Utilisateur user) {
        boolean hasActiveProjects = projetRepository
                .existsByPorteurIdAndStatutAndMontantActuelGreaterThan(
                        user.getId(),
                        StatutProjet.VALIDE,
                        java.math.BigDecimal.ZERO);

        if (hasActiveProjects) {
            throw new BusinessException(
                    "Impossible de supprimer : projets en cours avec investissements.");
        }

        utilisateurRepository.delete(user);
        log.info("Compte CREATOR supprimé — userId: {}", user.getId());
    }

    private Utilisateur getUserOrThrow(UUID userId) {
        return utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouvé avec l'id: " + userId));
    }

    private void anonymizeUser(Utilisateur user) {
        // ✅ Email unique pour éviter violation contrainte UNIQUE
        user.setEmail("deleted_" + user.getId() + "@deleted.com");
        user.setNom("Ancien utilisateur");
        user.setPrenom("Supprimé");          // ✅ Pas null si NOT NULL en base
        user.setMotDePasseHash(null);
        user.setOtpCode(null);
        user.setOtpExpiration(null);
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
    }

    private UserProfileResponse toProfileResponse(Utilisateur user) {
        boolean kycValide = user.getProfilKyc() != null
                && user.getProfilKyc().isKycValide();

        return UserProfileResponse.builder()
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .role(user.getRole().name())
                .statut(user.getStatut().name())
                .kycValide(kycValide)
                .createdAt(user.getCreatedAt())
                .build();
    }
}