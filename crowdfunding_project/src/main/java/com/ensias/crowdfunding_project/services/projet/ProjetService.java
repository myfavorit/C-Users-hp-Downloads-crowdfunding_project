package com.ensias.crowdfunding_project.services.projet;

import com.ensias.crowdfunding_project.dto.projet.ProjetRequest;
import com.ensias.crowdfunding_project.dto.projet.ProjetResponse;
import com.ensias.crowdfunding_project.entities.Projet;
import com.ensias.crowdfunding_project.entities.Utilisateur;
import com.ensias.crowdfunding_project.enums.DomaineProjet;
import com.ensias.crowdfunding_project.enums.StatutProjet;
import com.ensias.crowdfunding_project.repositories.ProjetRepository;
import com.ensias.crowdfunding_project.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final UtilisateurRepository utilisateurRepository;

    // ============================================================
    // 1. CRÉER UN PROJET (PROJECT_CREATOR only)
    // ============================================================

    @Transactional
    public ProjetResponse creerProjet(ProjetRequest request, UUID porteurId) {

        Utilisateur porteur = getUtilisateurActif(porteurId);

        if (!porteur.peutCreerProjet()) {
            throw new RuntimeException(
                    "Vous devez être PROJECT_CREATOR avec un KYC validé pour créer un projet"
            );
        }

        Projet projet = Projet.builder()
                .porteur(porteur)
                .titre(request.getTitre())
                .description(request.getDescription())
                .domaine(request.getDomaine())
                .objectifFinancier(request.getObjectifFinancier())
                .dureeJours(request.getDureeJours())
                .pourcentageOffert(request.getPourcentageOffert())
                .justificationValuation(request.getJustificationValuation())
                .imagePrincipale(request.getImagePrincipale())
                .statut(StatutProjet.BROUILLON)
                .build();

        Projet saved = projetRepository.save(projet);
        log.info("Projet créé: {} par {}", saved.getId(), porteurId);
        return ProjetResponse.from(saved);
    }

    // ============================================================
    // 2. SOUMETTRE UN PROJET (BROUILLON → EN_ATTENTE)
    // ============================================================

    @Transactional
    public ProjetResponse soumettreProjet(UUID projetId, UUID porteurId) {

        getUtilisateurActif(porteurId);
        Projet projet = getProjetOwnedBy(projetId, porteurId);

        // Uses business method from entity
        projet.soumettre();

        Projet saved = projetRepository.save(projet);
        log.info("Projet {} soumis pour validation", projetId);
        return ProjetResponse.from(saved);
    }

    // ============================================================
    // 3. MODIFIER UN PROJET (BROUILLON only)
    // ============================================================

    @Transactional
    public ProjetResponse modifierProjet(UUID projetId, ProjetRequest request, UUID porteurId) {

        getUtilisateurActif(porteurId);
        Projet projet = getProjetOwnedBy(projetId, porteurId);

        if (projet.getStatut() != StatutProjet.BROUILLON) {
            throw new RuntimeException("Seul un projet en brouillon peut être modifié");
        }

        projet.setTitre(request.getTitre());
        projet.setDescription(request.getDescription());
        projet.setDomaine(request.getDomaine());
        projet.setObjectifFinancier(request.getObjectifFinancier());
        projet.setDureeJours(request.getDureeJours());
        projet.setPourcentageOffert(request.getPourcentageOffert());
        projet.setJustificationValuation(request.getJustificationValuation());
        projet.setImagePrincipale(request.getImagePrincipale());

        Projet saved = projetRepository.save(projet);
        log.info("Projet {} modifié par {}", projetId, porteurId);
        return ProjetResponse.from(saved);
    }

    // ============================================================
    // 4. SUPPRIMER UN PROJET (soft delete)
    // ============================================================

    @Transactional
    public void supprimerProjet(UUID projetId, UUID porteurId) {

        getUtilisateurActif(porteurId);
        Projet projet = getProjetOwnedBy(projetId, porteurId);

        if (!projet.peutEtreSupprimePhysiquement()) {
            projet.softDelete();
            projetRepository.save(projet);
            log.info("Projet {} soft-deleted par {}", projetId, porteurId);
        } else {
            projetRepository.delete(projet);
            log.info("Projet {} hard-deleted par {}", projetId, porteurId);
        }
    }

    // ============================================================
    // 5. ANNULER UN PROJET (creator)
    // ============================================================

    @Transactional
    public ProjetResponse annulerProjet(UUID projetId, UUID porteurId) {

        getUtilisateurActif(porteurId);
        Projet projet = getProjetOwnedBy(projetId, porteurId);

        // Uses entity business method
        projet.annuler();

        Projet saved = projetRepository.save(projet);
        log.info("Projet {} annulé par {}", projetId, porteurId);
        return ProjetResponse.from(saved);
    }

    // ============================================================
    // 6. CLOTURER UN PROJET (admin or system)
    // ============================================================

    @Transactional
    public ProjetResponse cloturerProjet(UUID projetId, UUID adminId) {

        getAdminVerifie(adminId);

        Projet projet = projetRepository.findByIdAndIsDeletedFalse(projetId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        // Uses entity business method
        // → CLOTURE_SUCCES if goal reached, ECHEC_REMBOURSE if not
        projet.cloturer();

        Projet saved = projetRepository.save(projet);
        log.info("Projet {} clôturé par admin {} — statut: {}",
                projetId, adminId, saved.getStatut());
        return ProjetResponse.from(saved);
    }

    // ============================================================
    // 7. VALIDER UN PROJET (ADMIN — verified by ID in DB)
    // ============================================================

    @Transactional
    public ProjetResponse validerProjet(UUID projetId, UUID adminId) {

        // Double check: JWT says ADMIN + DB confirms it
        getAdminVerifie(adminId);

        Projet projet = projetRepository.findByIdAndIsDeletedFalse(projetId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        if (projet.getStatut() != StatutProjet.EN_ATTENTE) {
            throw new RuntimeException("Seul un projet EN_ATTENTE peut être validé");
        }

        // Uses entity business method
        projet.valider();

        Projet saved = projetRepository.save(projet);
        log.info("Projet {} validé par admin {}", projetId, adminId);
        return ProjetResponse.from(saved);
    }

    // ============================================================
    // 8. REJETER UN PROJET (ADMIN — verified by ID in DB)
    // ============================================================

    @Transactional
    public ProjetResponse rejeterProjet(UUID projetId, UUID adminId, String motifRefus) {

        // Double check: JWT says ADMIN + DB confirms it
        getAdminVerifie(adminId);

        if (motifRefus == null || motifRefus.isBlank()) {
            throw new RuntimeException("Le motif de refus est obligatoire");
        }

        Projet projet = projetRepository.findByIdAndIsDeletedFalse(projetId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        if (projet.getStatut() != StatutProjet.EN_ATTENTE) {
            throw new RuntimeException("Seul un projet EN_ATTENTE peut être rejeté");
        }

        // Uses entity business method
        projet.refuser();

        Projet saved = projetRepository.save(projet);
        log.info("Projet {} rejeté par admin {} — motif: {}",
                projetId, adminId, motifRefus);
        return ProjetResponse.from(saved);
    }

    // ============================================================
    // 9. LISTER LES PROJETS ACTIFS (public gallery)
    // ============================================================

    @Transactional(readOnly = true)
    public List<ProjetResponse> getProjetsActifs() {
        return projetRepository.findProjetsActifs(LocalDate.now())
                .stream()
                .map(ProjetResponse::from)
                .collect(Collectors.toList());
    }

    // ============================================================
    // 10. RECHERCHER PAR TITRE (public)
    // ============================================================

    @Transactional(readOnly = true)
    public List<ProjetResponse> rechercherParTitre(String motCle) {
        return projetRepository.rechercherParTitre(motCle)
                .stream()
                .map(ProjetResponse::from)
                .collect(Collectors.toList());
    }

    // ============================================================
    // 11. DÉTAIL D'UN PROJET (public)
    // ============================================================

    @Transactional(readOnly = true)
    public ProjetResponse getProjetById(UUID projetId) {
        Projet projet = projetRepository.findByIdAndIsDeletedFalse(projetId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));
        return ProjetResponse.from(projet);
    }

    // ============================================================
    // 12. MES PROJETS (creator dashboard)
    // ============================================================

    @Transactional(readOnly = true)
    public List<ProjetResponse> getMesProjets(UUID porteurId) {
        getUtilisateurActif(porteurId);
        return projetRepository.findByPorteurIdAndIsDeletedFalse(porteurId)
                .stream()
                .map(ProjetResponse::from)
                .collect(Collectors.toList());
    }

    // ============================================================
    // 13. MES PROJETS PAR STATUT (creator dashboard filter)
    // ============================================================

    @Transactional(readOnly = true)
    public List<ProjetResponse> getMesProjetsParStatut(UUID porteurId, StatutProjet statut) {
        getUtilisateurActif(porteurId);
        return projetRepository
                .findByPorteurIdAndStatutAndIsDeletedFalse(porteurId, statut)
                .stream()
                .map(ProjetResponse::from)
                .collect(Collectors.toList());
    }

    // ============================================================
    // 14. PROJETS EN ATTENTE (admin dashboard)
    // ============================================================

    @Transactional(readOnly = true)
    public List<ProjetResponse> getProjetsEnAttente(UUID adminId) {
        getAdminVerifie(adminId);
        return projetRepository
                .findByStatutAndIsDeletedFalse(StatutProjet.EN_ATTENTE)
                .stream()
                .map(ProjetResponse::from)
                .collect(Collectors.toList());
    }

    // ============================================================
    // 15. PROJETS PAR DOMAINE (public filter)
    // ============================================================

    @Transactional(readOnly = true)
    public List<ProjetResponse> getProjetsByDomaine(DomaineProjet domaine) {
        return projetRepository.findProjetsActifs(LocalDate.now())
                .stream()
                .filter(p -> p.getDomaine() == domaine)
                .map(ProjetResponse::from)
                .collect(Collectors.toList());
    }

    // ============================================================
    // PRIVATE HELPERS
    // ============================================================

    private Utilisateur getUtilisateurActif(UUID userId) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        if (!utilisateur.isActif()) {
            throw new RuntimeException("Compte suspendu ou banni");
        }
        return utilisateur;
    }

    /**
     * DB double-check for admin
     * JWT says ADMIN + DB confirms role=ADMIN AND statut=ACTIF
     */
    private Utilisateur getAdminVerifie(UUID adminId) {
        Utilisateur admin = utilisateurRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin introuvable"));
        if (!admin.isAdmin()) {
            log.warn("Tentative d'accès admin refusée pour userId: {}", adminId);
            throw new RuntimeException("Accès refusé — droits administrateur requis");
        }
        if (!admin.isActif()) {
            log.warn("Compte admin inactif: {}", adminId);
            throw new RuntimeException("Compte administrateur suspendu ou banni");
        }
        return admin;
    }

    private Projet getProjetOwnedBy(UUID projetId, UUID porteurId) {
        Projet projet = projetRepository.findByIdAndIsDeletedFalse(projetId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));
        if (!projet.getPorteur().getId().equals(porteurId)) {
            throw new RuntimeException("Vous n'êtes pas le porteur de ce projet");
        }
        return projet;
    }
}