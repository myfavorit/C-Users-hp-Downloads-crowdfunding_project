package com.ensias.crowdfunding_project.services.dashboard;

import com.ensias.crowdfunding_project.dto.dashboard.AdminDashboardResponse;
import com.ensias.crowdfunding_project.dto.dashboard.InvestorDashboardResponse;
import com.ensias.crowdfunding_project.dto.dashboard.CreatorDashboardResponse;
import com.ensias.crowdfunding_project.entities.Utilisateur;
import com.ensias.crowdfunding_project.enums.RoleUtilisateur;
import com.ensias.crowdfunding_project.enums.StatutProjet;
import com.ensias.crowdfunding_project.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProjetRepository projetRepository;
    private final InvestissementRepository investissementRepository;
    private final FavoriRepository favoriRepository;
    private final ValidationRepository validationRepository;
    private final ProfilKycRepository profilKycRepository;

    // ============================================================
    // 1. ADMIN DASHBOARD
    // ============================================================

    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboard(UUID adminId) {

        // Verify admin in DB
        Utilisateur admin = getAdminVerifie(adminId);
        log.info("Admin dashboard généré pour : {} {}", admin.getEmail(), admin.getRole());

        // ── Users stats ──────────────────────────────────────────
        // FIX: Use RoleUtilisateur directly, not Utilisateur.role
        long totalInvestors = utilisateurRepository
                .findByRole(RoleUtilisateur.INVESTOR).size();
        long totalCreators = utilisateurRepository
                .findByRole(RoleUtilisateur.PROJECT_CREATOR).size();
        long totalAdmins = utilisateurRepository
                .findByRole(RoleUtilisateur.ADMIN).size();
        long totalUsers = totalInvestors + totalCreators + totalAdmins;

        // ── Projects stats ───────────────────────────────────────
        long totalProjets = projetRepository.count();
        long projetsEnAttente = projetRepository
                .countByStatutAndIsDeletedFalse(StatutProjet.EN_ATTENTE);
        long projetsValides = projetRepository
                .countByStatutAndIsDeletedFalse(StatutProjet.VALIDE);
        long projetsRejetes = projetRepository
                .countByStatutAndIsDeletedFalse(StatutProjet.REJETE);
        long projetsClotures = projetRepository
                .countByStatutAndIsDeletedFalse(StatutProjet.CLOTURE_SUCCES);

        // ── Financial stats ──────────────────────────────────────
        BigDecimal montantTotalCollecte = projetRepository.montantTotalCollecte();
        BigDecimal montantTotalInvesti  = investissementRepository.sumMontantTotal();

        // ── Validation stats ─────────────────────────────────────
        Double tauxApprobation = validationRepository.getTauxApprobation();

        // ── KYC stats ────────────────────────────────────────────
        long kycEnAttente = profilKycRepository.countKycEnAttenteForActiveUsers();
        long kycValides   = profilKycRepository.countValidKycForActiveUsers();

        // ── Investment stats ─────────────────────────────────────
        long totalInvestissements = investissementRepository.count();

        log.info("Admin dashboard chargé par {}", adminId);

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalInvestors(totalInvestors)
                .totalCreators(totalCreators)
                .totalAdmins(totalAdmins)
                .totalProjets(totalProjets)
                .projetsEnAttente(projetsEnAttente)
                .projetsValides(projetsValides)
                .projetsRejetes(projetsRejetes)
                .projetsClotures(projetsClotures)
                .montantTotalCollecte(montantTotalCollecte != null ? montantTotalCollecte : BigDecimal.ZERO)
                .montantTotalInvesti(montantTotalInvesti != null ? montantTotalInvesti : BigDecimal.ZERO)
                .tauxApprobation(tauxApprobation != null ? tauxApprobation : 0.0)
                .kycEnAttente(kycEnAttente)
                .kycValides(kycValides)
                .totalInvestissements(totalInvestissements)
                .build();
    }

    // ============================================================
    // 2. INVESTOR DASHBOARD
    // ============================================================

    @Transactional(readOnly = true)
    public InvestorDashboardResponse getInvestorDashboard(UUID investisseurId) {

        Utilisateur investisseur = getUtilisateurActif(investisseurId);

        BigDecimal totalInvesti = investissementRepository
                .sumMontantParInvestisseur(investisseurId);

        long nombreInvestissements = investissementRepository
                .findByInvestisseurIdOrderByCreatedAtDesc(investisseurId)
                .size();

        long projetsFinances = investissementRepository
                .findByInvestisseurIdOrderByCreatedAtDesc(investisseurId)
                .stream()
                .map(inv -> inv.getProjet().getId())
                .distinct()
                .count();

        // FIX: removed unused parameter 'totalInvesti'
        BigDecimal partMoyenne = calculerPartMoyenne(investisseurId);

        long nombreFavoris = favoriRepository
                .findByUtilisateurIdOrderByCreatedAtDesc(investisseurId)
                .size();

        boolean kycValide = investisseur.hasKycValide();

        // Last 5 investments
        List<InvestorDashboardResponse.InvestissementSummary> dernierInvestissements =
                investissementRepository
                        .findByInvestisseurIdOrderByCreatedAtDesc(investisseurId)
                        .stream()
                        .limit(5)
                        .map(inv -> InvestorDashboardResponse.InvestissementSummary.builder()
                                .projetId(inv.getProjet().getId())
                                .projetTitre(inv.getProjet().getTitre())
                                .montant(inv.getMontant())
                                .statutPaiement(inv.getStatutPaiement().name())
                                .createdAt(inv.getCreatedAt())
                                .build()
                        )
                        .collect(Collectors.toList());

        // Top 3 favorites
        List<InvestorDashboardResponse.FavoriSummary> mesFavoris =
                favoriRepository
                        .findByUtilisateurIdWithProjet(investisseurId)
                        .stream()
                        .limit(3)
                        .map(fav -> InvestorDashboardResponse.FavoriSummary.builder()
                                .projetId(fav.getProjet().getId())
                                .projetTitre(fav.getProjet().getTitre())
                                .pourcentageFinancement(fav.getProjet().getPourcentageFinancement())
                                .montantActuel(fav.getProjet().getMontantActuel())
                                .objectifFinancier(fav.getProjet().getObjectifFinancier())
                                .build()
                        )
                        .collect(Collectors.toList());

        log.info("Investor dashboard chargé par {}", investisseurId);

        return InvestorDashboardResponse.builder()
                .totalInvesti(totalInvesti != null ? totalInvesti : BigDecimal.ZERO)
                .nombreInvestissements(nombreInvestissements)
                .projetsFinances(projetsFinances)
                .partMoyenne(partMoyenne)
                .nombreFavoris(nombreFavoris)
                .kycValide(kycValide)
                .dernierInvestissements(dernierInvestissements)
                .mesFavoris(mesFavoris)
                .build();
    }

    // ============================================================
    // 3. CREATOR DASHBOARD
    // ============================================================

    @Transactional(readOnly = true)
    public CreatorDashboardResponse getCreatorDashboard(UUID porteurId) {

        Utilisateur porteur = getUtilisateurActif(porteurId);

        var mesProjets = projetRepository
                .findByPorteurIdAndIsDeletedFalse(porteurId);

        long totalProjets    = mesProjets.size();
        long projetsEnAttente = mesProjets.stream()
                .filter(p -> p.getStatut() == StatutProjet.EN_ATTENTE)
                .count();
        long projetsValides  = mesProjets.stream()
                .filter(p -> p.getStatut() == StatutProjet.VALIDE)
                .count();
        long projetsRejetes  = mesProjets.stream()
                .filter(p -> p.getStatut() == StatutProjet.REJETE)
                .count();
        long projetsClotures = mesProjets.stream()
                .filter(p -> p.getStatut() == StatutProjet.CLOTURE_SUCCES)
                .count();

        BigDecimal montantCollecte = mesProjets.stream()
                .map(p -> p.getMontantActuel() != null ? p.getMontantActuel() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long nombreInvestisseurs = mesProjets.stream()
                .mapToLong(p -> investissementRepository.countInvestisseursParProjet(p.getId()))
                .sum();

        boolean kycValide = porteur.hasKycValide();

        // Top 3 projects by funding %
        List<CreatorDashboardResponse.ProjetSummary> topProjets = mesProjets
                .stream()
                .sorted((a, b) -> b.getPourcentageFinancement().compareTo(a.getPourcentageFinancement()))
                .limit(3)
                .map(p -> CreatorDashboardResponse.ProjetSummary.builder()
                        .projetId(p.getId())
                        .titre(p.getTitre())
                        .montantActuel(p.getMontantActuel())
                        .objectifFinancier(p.getObjectifFinancier())
                        .pourcentageFinancement(p.getPourcentageFinancement())
                        .statut(p.getStatut().name())
                        .build()
                )
                .collect(Collectors.toList());

        // Last 5 investments across all projects
        List<CreatorDashboardResponse.InvestissementRecuSummary> derniersInvestissements =
                mesProjets.stream()
                        .flatMap(p -> investissementRepository
                                .findByProjetIdOrderByCreatedAtDesc(p.getId())
                                .stream()
                                .limit(5))
                        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                        .limit(5)
                        .map(inv -> CreatorDashboardResponse.InvestissementRecuSummary.builder()
                                .investisseurNom(inv.getInvestisseur().getNom())
                                .investisseurPrenom(inv.getInvestisseur().getPrenom())
                                .projetTitre(inv.getProjet().getTitre())
                                .montant(inv.getMontant())
                                .createdAt(inv.getCreatedAt())
                                .build()
                        )
                        .collect(Collectors.toList());

        CreatorDashboardResponse.StatutBreakdown statutBreakdown =
                CreatorDashboardResponse.StatutBreakdown.builder()
                        .enAttente(projetsEnAttente)
                        .valides(projetsValides)
                        .rejetes(projetsRejetes)
                        .clotures(projetsClotures)
                        .build();

        log.info("Creator dashboard chargé par {}", porteurId);

        return CreatorDashboardResponse.builder()
                .totalProjets(totalProjets)
                .projetsEnAttente(projetsEnAttente)
                .projetsValides(projetsValides)
                .montantCollecte(montantCollecte)
                .nombreInvestisseurs(nombreInvestisseurs)
                .kycValide(kycValide)
                .statutBreakdown(statutBreakdown)
                .topProjets(topProjets)
                .derniersInvestissementsRecus(derniersInvestissements)
                .build();
    }

    // ============================================================
    // PRIVATE HELPERS
    // ============================================================

    private Utilisateur getAdminVerifie(UUID adminId) {
        Utilisateur admin = utilisateurRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin introuvable"));
        if (!admin.isAdmin()) {
            throw new RuntimeException("Accès refusé — droits administrateur requis");
        }
        if (!admin.isActif()) {
            throw new RuntimeException("Compte administrateur suspendu ou banni");
        }
        return admin;
    }

    private Utilisateur getUtilisateurActif(UUID userId) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        if (!utilisateur.isActif()) {
            throw new RuntimeException("Compte suspendu ou banni");
        }
        return utilisateur;
    }

    /**
     * Calcul de la part moyenne en capital (equity) sur tous les investissements.
     * Formule = somme( (montant_investi / objectif_projet) * pourcentage_offert ) / nombre_investissements
     */
    private BigDecimal calculerPartMoyenne(UUID investisseurId) {
        var investissements = investissementRepository
                .findByInvestisseurIdOrderByCreatedAtDesc(investisseurId);

        if (investissements.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalParts = investissements.stream()
                .filter(inv -> inv.getProjet().getPourcentageOffert() != null
                        && inv.getProjet().getObjectifFinancier() != null
                        && inv.getProjet().getObjectifFinancier().compareTo(BigDecimal.ZERO) > 0)
                .map(inv -> inv.getMontant()
                        .divide(inv.getProjet().getObjectifFinancier(), 10, RoundingMode.HALF_UP)
                        .multiply(inv.getProjet().getPourcentageOffert()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalParts.divide(BigDecimal.valueOf(investissements.size()), 2, RoundingMode.HALF_UP);
    }
}