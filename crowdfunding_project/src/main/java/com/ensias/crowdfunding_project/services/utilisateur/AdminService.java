package com.ensias.crowdfunding_project.services.utilisateur;

import com.ensias.crowdfunding_project.dto.utilisateur.admin.KycPendingResponse;
import com.ensias.crowdfunding_project.dto.utilisateur.admin.projet.ProjetAdminResponse;
import com.ensias.crowdfunding_project.repositories.InvestissementRepository;
import com.ensias.crowdfunding_project.dto.utilisateur.admin.AdminDashboardStats;
import com.ensias.crowdfunding_project.dto.utilisateur.admin.UserResponse;
import com.ensias.crowdfunding_project.entities.*;
import com.ensias.crowdfunding_project.enums.RoleUtilisateur;
import com.ensias.crowdfunding_project.enums.StatutCompte;
import com.ensias.crowdfunding_project.enums.StatutPaiement;
import com.ensias.crowdfunding_project.enums.StatutProjet;
import com.ensias.crowdfunding_project.exception.BusinessException;
import com.ensias.crowdfunding_project.exception.ResourceNotFoundException;
import com.ensias.crowdfunding_project.repositories.*;
import com.ensias.crowdfunding_project.services.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProjetRepository projetRepository;
    private final ValidationRepository validationRepository;
    private final NotificationService notificationService;
    private final ProfilKycRepository profilKycRepository;
    private final InvestissementRepository investissementRepository;

    // ========================= PROJETS =========================
    public Page<Projet> getProjetsEnAttente(Pageable pageable) {
        return projetRepository.findByStatutAndIsDeletedFalse(StatutProjet.EN_ATTENTE, pageable);
    }

    public void approuverProjet(UUID projetId, UUID adminId) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouvé"));

        if (projet.getStatut() != StatutProjet.EN_ATTENTE) {
            throw new BusinessException("Seul un projet en attente peut être approuvé");
        }
        if (!projet.isComplet()) {
            throw new BusinessException("Le projet n'est pas complet. Demandez au porteur de le compléter.");
        }
        if (!projet.getPorteur().hasKycValide()) {
            throw new BusinessException("Le porteur du projet n'a pas de KYC validé. Impossible d'approuver.");
        }

        projet.valider();
        projetRepository.save(projet);

        Validation validation = Validation.builder()
                .projet(projet)
                .admin(utilisateurRepository.getReferenceById(adminId))
                .decision(Validation.Decision.VALIDE)
                .build();
        validationRepository.save(validation);

        notificationService.notifierProjetValide(projet.getPorteur(), projet.getTitre());
        log.info("Admin {} a approuvé le projet {}", adminId, projetId);
    }

    public void refuserProjet(UUID projetId, UUID adminId, String motif) {
        if (motif == null || motif.isBlank()) {
            throw new BusinessException("Le motif du refus est obligatoire");
        }
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouvé"));
        if (projet.getStatut() != StatutProjet.EN_ATTENTE) {
            throw new BusinessException("Seul un projet en attente peut être refusé");
        }
        projet.refuser();
        projetRepository.save(projet);

        Validation validation = Validation.builder()
                .projet(projet)
                .admin(utilisateurRepository.getReferenceById(adminId))
                .decision(Validation.Decision.REJETE)
                .motifRefus(motif)
                .build();
        validationRepository.save(validation);

        notificationService.notifierProjetRejete(projet.getPorteur(), projet.getTitre(), motif);
        log.info("Admin {} a refusé le projet {} (motif : {})", adminId, projetId, motif);
    }

    // ========================= UTILISATEURS =========================
    public Page<UserResponse> getAllUtilisateurs(Pageable pageable, String role, String statut) {
        Page<Utilisateur> page;
        if (role != null && statut != null) {
            page = utilisateurRepository.findByRoleAndStatut(
                    RoleUtilisateur.valueOf(role), StatutCompte.valueOf(statut), pageable);
        } else if (role != null) {
            page = utilisateurRepository.findByRole(RoleUtilisateur.valueOf(role), pageable);
        } else if (statut != null) {
            page = utilisateurRepository.findByStatut(StatutCompte.valueOf(statut), pageable);
        } else {
            page = utilisateurRepository.findAll(pageable);
        }
        return page.map(this::toUserResponse);
    }

    public UserResponse getUtilisateurDetail(UUID userId) {
        return toUserResponse(getUtilisateurEntity(userId));
    }

    public void modifierStatutUtilisateur(UUID userId, StatutCompte nouveauStatut, UUID adminId, String motif) {
        if ((nouveauStatut == StatutCompte.SUSPENDU || nouveauStatut == StatutCompte.BANNI)
                && (motif == null || motif.isBlank())) {
            throw new BusinessException("Un motif est obligatoire pour suspendre ou bannir");
        }
        Utilisateur user = getUtilisateurEntity(userId);
        user.setStatut(nouveauStatut);
        utilisateurRepository.save(user);
        notificationService.notifierChangementStatut(user, nouveauStatut, motif);
        log.info("Admin {} a changé le statut de {} en {}", adminId, userId, nouveauStatut);
    }

    // Optionnel : méthode pour modifier le rôle (si vous la réactivez)
    /*
    public void modifierRoleUtilisateur(UUID userId, RoleUtilisateur nouveauRole, UUID adminId) {
        Utilisateur user = getUtilisateurEntity(userId);
        user.setRole(nouveauRole);
        utilisateurRepository.save(user);
        log.info("Admin {} a changé le rôle de {} en {}", adminId, userId, nouveauRole);
    }
    */

    public void supprimerUtilisateurPhysiquement(UUID userId, UUID adminId) {
        Utilisateur user = getUtilisateurEntity(userId);
        if (!user.getProjets().isEmpty() || !user.getInvestissements().isEmpty()) {
            throw new BusinessException("Impossible de supprimer un utilisateur avec des projets ou investissements");
        }
        utilisateurRepository.delete(user);
        log.warn("Admin {} a supprimé physiquement l'utilisateur {}", adminId, userId);
    }

    // ========================= STATISTIQUES =========================
    public AdminDashboardStats getDashboardStats() {
        // --- Utilisateurs ---
        long totalUtilisateurs = utilisateurRepository.count();
        long utilisateursActifs = utilisateurRepository.countByStatut(StatutCompte.ACTIF);
        long utilisateursSuspendus = utilisateurRepository.countByStatut(StatutCompte.SUSPENDU);
        long utilisateursBannis = utilisateurRepository.countByStatut(StatutCompte.BANNI);

        // --- Projets ---
        long projetsEnAttente = projetRepository.countByStatutAndIsDeletedFalse(StatutProjet.EN_ATTENTE);
        long projetsValides = projetRepository.countByStatutAndIsDeletedFalse(StatutProjet.VALIDE);

        // --- KYC ---
        long kycEnAttente = profilKycRepository.countByKycValideFalseAndKycSoumisAtIsNotNull();

        // --- Financement global ---
        BigDecimal montantTotalCollecte = projetRepository.montantTotalCollecte();

        // --- 🆕 INVESTISSEMENTS (appels corrigés) ---
        long totalInvestissements = investissementRepository.countByStatutPaiement(StatutPaiement.CONFIRME);
        BigDecimal montantTotalInvesti = investissementRepository.sumMontantTotal(); // ← bonne méthode
        BigDecimal montantMoyenInvestissement = investissementRepository.avgMontantInvestissement();
        long totalInvestisseurs = investissementRepository.countDistinctInvestisseurs();

        return AdminDashboardStats.builder()
                .totalUtilisateurs(totalUtilisateurs)
                .utilisateursActifs(utilisateursActifs)
                .utilisateursSuspendus(utilisateursSuspendus)
                .utilisateursBannis(utilisateursBannis)
                .projetsEnAttente(projetsEnAttente)
                .projetsValides(projetsValides)
                .kycEnAttente(kycEnAttente)
                .montantTotalCollecte(montantTotalCollecte)
                .totalInvestissements(totalInvestissements)
                .montantTotalInvesti(montantTotalInvesti)
                .montantMoyenInvestissement(montantMoyenInvestissement)
                .totalInvestisseurs(totalInvestisseurs)
                .build();
    }
    public List<ProjetAdminResponse> getLastProjects(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return projetRepository.findAllByOrderByCreatedAtDesc(pageable)
                .stream()
                .map(this::toProjetAdminResponse)   // ← nom correct avec 'j'
                .toList();
    }
    public List<KycPendingResponse> getLastPendingKyc(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return profilKycRepository.findByKycValideFalseAndKycSoumisAtIsNotNullOrderByKycSoumisAtDesc(pageable)
                .stream()
                .map(this::toKycPendingResponse)
                .toList();
    }

    private KycPendingResponse toKycPendingResponse(ProfilKyc kyc) {
        Utilisateur user = kyc.getUtilisateur();
        return KycPendingResponse.builder()
                .kycId(kyc.getId())
                .utilisateurNom(user.getNom())
                .utilisateurPrenom(user.getPrenom())
                .utilisateurEmail(user.getEmail())
                .kycSoumisAt(kyc.getKycSoumisAt())
                .build();
    }

    // Méthode privée de mapping (identique à celle utilisée dans AdminProjetController)
    private ProjetAdminResponse toProjetAdminResponse(Projet projet) {
        return ProjetAdminResponse.builder()
                .id(projet.getId())
                .titre(projet.getTitre())
                .description(projet.getDescription())
                .domaine(projet.getDomaine().name())
                .objectifFinancier(projet.getObjectifFinancier())
                .montantActuel(projet.getMontantActuel())
                .dateDebut(projet.getDateDebut())
                .dateFin(projet.getDateFin())
                .statut(projet.getStatut())
                .porteurId(projet.getPorteur().getId())
                .porteurNom(projet.getPorteur().getNom())
                .porteurPrenom(projet.getPorteur().getPrenom())
                .createdAt(projet.getCreatedAt())
                .build();
    }
    // ========================= MÉTHODES PRIVÉES =========================
    private Utilisateur getUtilisateurEntity(UUID userId) {
        return utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    }

    // ✅ À ajouter dans AdminService — évite un scan complet dans le contrôleur
    public Projet findProjetEnAttente(UUID projetId) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouvé"));

        // Sécurité : on ne laisse analyser que les projets EN_ATTENTE
        if (projet.getStatut() != StatutProjet.EN_ATTENTE) {
            throw new BusinessException("Ce projet n'est pas en attente de validation.");
        }
        return projet;
    }

    private UserResponse toUserResponse(Utilisateur user) {
        boolean kycValide = user.getProfilKyc() != null && user.getProfilKyc().isKycValide();
        return UserResponse.builder()
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .role(user.getRole().name())
                .statut(user.getStatut().name())
                .createdAt(user.getCreatedAt())
                .kycValide(kycValide)
                .build();
    }
}