package com.ensias.crowdfunding_project.services.investissement;

import com.ensias.crowdfunding_project.dto.investissement.InvestissementRequest;
import com.ensias.crowdfunding_project.dto.investissement.InvestissementResponse;
import com.ensias.crowdfunding_project.entities.*;
import com.ensias.crowdfunding_project.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestissementService {

    private final InvestissementRepository investissementRepository;
    private final ProjetRepository projetRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationRepository notificationRepository;

    // ============================================================
    // 1. INVESTIR DANS UN PROJET (INVESTOR only)
    // ============================================================

    @Transactional
    public InvestissementResponse investir(InvestissementRequest request, UUID investisseurId) {

        // 1. Verify investor exists and is active
        Utilisateur investisseur = getInvestisseurVerifie(investisseurId);

        // 2. Verify investor can invest (KYC + role)
        if (!investisseur.peutInvestir()) {
            throw new RuntimeException(
                    "Vous devez avoir un KYC validé pour investir"
            );
        }

        // 3. Find project
        Projet projet = projetRepository
                .findByIdAndIsDeletedFalse(request.getProjetId())
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        // 4. Check project is open
        if (!projet.estOuvert()) {
            throw new RuntimeException(
                    "Ce projet n'est pas ouvert aux investissements"
            );
        }

        // 5. Validate amount
        if (request.getMontant() == null ||
                request.getMontant().compareTo(BigDecimal.valueOf(10)) < 0) {
            throw new RuntimeException("Le montant minimum d'investissement est de 10 MAD");
        }

        // 6. Check amount doesn't exceed remaining goal
        BigDecimal montantRestant = projet.getMontantRestant();
        if (request.getMontant().compareTo(montantRestant) > 0) {
            throw new RuntimeException(
                    "Le montant dépasse le reste à financer (" + montantRestant + " MAD)"
            );
        }

        // 7. Generate unique reference
        String reference = genererReference();

        // 8. Build investment
        Investissement investissement = Investissement.builder()
                .investisseur(investisseur)
                .projet(projet)
                .montant(request.getMontant())
                .modePaiement(
                        request.getModePaiement() != null
                                ? request.getModePaiement()
                                : Investissement.ModePaiement.SIMULATION
                )
                .referencePaiement(reference)
                .statutPaiement(Investissement.StatutPaiement.EN_ATTENTE)
                .build();

        // 9. Save investment
        Investissement saved = investissementRepository.save(investissement);

        // 10. Simulate payment confirmation
        confirmerPaiement(saved.getId(), investisseurId);

        log.info("Investissement {} créé par {} dans projet {}",
                saved.getId(), investisseurId, request.getProjetId());

        return InvestissementResponse.from(saved, projet.getPourcentageOffert(),
                projet.getObjectifFinancier());
    }

    // ============================================================
    // 2. CONFIRMER LE PAIEMENT (simulation)
    // ============================================================

    @Transactional
    public InvestissementResponse confirmerPaiement(UUID investissementId, UUID investisseurId) {

        // 1. Find investment
        Investissement investissement = investissementRepository.findById(investissementId)
                .orElseThrow(() -> new RuntimeException("Investissement introuvable"));

        // 2. Verify ownership
        if (!investissement.getInvestisseur().getId().equals(investisseurId)) {
            throw new RuntimeException("Accès refusé");
        }

        // 3. Confirm payment using entity method
        investissement.confirmer();
        Investissement saved = investissementRepository.save(investissement);

        // 4. Update project amount
        Projet projet = investissement.getProjet();
        projet.ajouterInvestissement(investissement.getMontant());
        projetRepository.save(projet);

        // 5. Notify project creator
        envoyerNotificationInvestissement(investissement, projet);

        log.info("Paiement confirmé pour investissement {}", investissementId);

        return InvestissementResponse.from(saved, projet.getPourcentageOffert(),
                projet.getObjectifFinancier());
    }

    // ============================================================
    // 3. MES INVESTISSEMENTS (investor dashboard)
    // ============================================================

    @Transactional(readOnly = true)
    public List<InvestissementResponse> getMesInvestissements(UUID investisseurId) {

        getInvestisseurVerifie(investisseurId);

        return investissementRepository
                .findByInvestisseurIdOrderByCreatedAtDesc(investisseurId)
                .stream()
                .map(inv -> InvestissementResponse.from(
                        inv,
                        inv.getProjet().getPourcentageOffert(),
                        inv.getProjet().getObjectifFinancier()
                ))
                .collect(Collectors.toList());
    }

    // ============================================================
    // 4. INVESTISSEMENTS D'UN PROJET (creator/admin)
    // ============================================================

    @Transactional(readOnly = true)
    public List<InvestissementResponse> getInvestissementsParProjet(
            UUID projetId, UUID demandeurId) {

        // Verify demandeur exists
        Utilisateur demandeur = utilisateurRepository.findById(demandeurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Find project
        Projet projet = projetRepository.findByIdAndIsDeletedFalse(projetId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        // Only the creator or admin can see all investments of a project
        boolean isAdmin   = demandeur.isAdmin();
        boolean isCreator = projet.getPorteur().getId().equals(demandeurId);

        if (!isAdmin && !isCreator) {
            throw new RuntimeException("Accès refusé");
        }

        return investissementRepository
                .findByProjetIdOrderByCreatedAtDesc(projetId)
                .stream()
                .map(inv -> InvestissementResponse.from(
                        inv,
                        projet.getPourcentageOffert(),
                        projet.getObjectifFinancier()
                ))
                .collect(Collectors.toList());
    }

    // ============================================================
    // 5. DÉTAIL D'UN INVESTISSEMENT
    // ============================================================

    @Transactional(readOnly = true)
    public InvestissementResponse getInvestissementById(
            UUID investissementId, UUID demandeurId) {

        Investissement investissement = investissementRepository
                .findById(investissementId)
                .orElseThrow(() -> new RuntimeException("Investissement introuvable"));

        Utilisateur demandeur = utilisateurRepository.findById(demandeurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Only investor himself or admin can see
        boolean isOwner = investissement.getInvestisseur().getId().equals(demandeurId);
        boolean isAdmin = demandeur.isAdmin();

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Accès refusé");
        }

        Projet projet = investissement.getProjet();
        return InvestissementResponse.from(
                investissement,
                projet.getPourcentageOffert(),
                projet.getObjectifFinancier()
        );
    }

    // ============================================================
    // 6. MONTANT TOTAL INVESTI PAR UN INVESTISSEUR
    // ============================================================

    @Transactional(readOnly = true)
    public BigDecimal getMontantTotalInvesti(UUID investisseurId) {
        getInvestisseurVerifie(investisseurId);
        return investissementRepository.sumMontantParInvestisseur(investisseurId);
    }

    // ============================================================
    // 7. STATISTIQUES D'UN PROJET
    // ============================================================

    @Transactional(readOnly = true)
    public ProjetInvestissementStats getStatsProjet(UUID projetId, UUID adminId) {

        // Admin only
        Utilisateur admin = utilisateurRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        if (!admin.isAdmin()) {
            throw new RuntimeException("Accès refusé");
        }

        BigDecimal montantTotal = investissementRepository.sumMontantParProjet(projetId);
        long nbInvestisseurs    = investissementRepository.countInvestisseursParProjet(projetId);

        return new ProjetInvestissementStats(projetId, montantTotal, nbInvestisseurs);
    }

    // ============================================================
    // PRIVATE HELPERS
    // ============================================================

    /**
     * Verify investor exists, is active, and has INVESTOR role
     */
    private Utilisateur getInvestisseurVerifie(UUID investisseurId) {
        Utilisateur investisseur = utilisateurRepository.findById(investisseurId)
                .orElseThrow(() -> new RuntimeException("Investisseur introuvable"));

        if (!investisseur.isActif()) {
            throw new RuntimeException("Compte suspendu ou banni");
        }

        if (!investisseur.isInvestor()) {
            throw new RuntimeException("Seul un INVESTOR peut effectuer cette action");
        }

        return investisseur;
    }

    /**
     * Generate unique payment reference
     * Format: INV-XXXXXXXX (8 random uppercase chars)
     */
    private String genererReference() {
        String reference;
        do {
            String uuid = UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();
            reference = "INV-" + uuid;
        } while (investissementRepository.findByReferencePaiement(reference).isPresent());

        return reference;
    }

    /**
     * Send notification to project creator when investment is received
     */
    private void envoyerNotificationInvestissement(
            Investissement investissement, Projet projet) {

        Utilisateur porteur      = projet.getPorteur();
        Utilisateur investisseur = investissement.getInvestisseur();

        String nomInvestisseur = investisseur.getPrenom() + " " + investisseur.getNom();
        String montantStr      = investissement.getMontant().toString();

        Notification notification = Notification.investissementRecu(
                porteur,
                projet.getTitre(),
                montantStr,
                nomInvestisseur
        );

        notificationRepository.save(notification);
        log.info("Notification envoyée au porteur {} pour investissement dans {}",
                porteur.getId(), projet.getTitre());
    }

    // ============================================================
    // INNER CLASS — Stats DTO
    // ============================================================

    public record ProjetInvestissementStats(
            UUID projetId,
            BigDecimal montantTotal,
            long nbInvestisseurs
    ) {}
}