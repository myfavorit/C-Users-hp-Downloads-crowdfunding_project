package com.ensias.crowdfunding_project.services.utilisateur;

import com.ensias.crowdfunding_project.dto.utilisateur.user.*;
import com.ensias.crowdfunding_project.entities.Favori;
import com.ensias.crowdfunding_project.entities.Investissement;
import com.ensias.crowdfunding_project.entities.Projet;
import com.ensias.crowdfunding_project.entities.Utilisateur;
import com.ensias.crowdfunding_project.enums.StatutPaiement;
import com.ensias.crowdfunding_project.enums.StatutProjet;
import com.ensias.crowdfunding_project.exception.BusinessException;
import com.ensias.crowdfunding_project.exception.ResourceNotFoundException;
import com.ensias.crowdfunding_project.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserDashboardService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProjetRepository projetRepository;
    private final InvestissementRepository investissementRepository;
    private final NotificationRepository notificationRepository;
    private final FavoriRepository favoriRepository;

    // ================================================================
    // POINT D'ENTREE PRINCIPAL
    // ================================================================

    public UserDashboardResponse getDashboard(UUID userId) {
        log.info("Récupération dashboard pour userId: {}", userId);

        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouvé avec l'id: " + userId));

        boolean kycValide = user.getProfilKyc() != null
                && user.getProfilKyc().isKycValide();

        long notificationsNonLues = notificationRepository
                .countByDestinataireIdAndLuFalse(userId);

        // ✅ Switch sur l'enum directement
        return switch (user.getRole()) {
            case INVESTOR ->
                    buildInvestorDashboard(userId, kycValide, notificationsNonLues);
            case PROJECT_CREATOR ->
                    buildCreatorDashboard(userId, kycValide, notificationsNonLues);
            default ->
                    throw new BusinessException(
                            "Dashboard non disponible pour le rôle: " + user.getRole());
        };
    }

    // ================================================================
    // DASHBOARD INVESTISSEUR
    // ================================================================

    private UserDashboardResponse buildInvestorDashboard(
            UUID userId,
            boolean kycValide,
            long notificationsNonLues) {

        log.info("Construction dashboard INVESTOR pour userId: {}", userId);

        // ── Stats principales ──────────────────────────────────

        // ✅ Null safety sur les agrégats
        BigDecimal totalInvesti = Optional.ofNullable(
                        investissementRepository.sumMontantParInvestisseur(userId))
                .orElse(BigDecimal.ZERO);

        long nombreInvestissements = investissementRepository
                .countByInvestisseurIdAndStatutPaiement(
                        userId, StatutPaiement.CONFIRME);

        long nombreProjetsFinances = investissementRepository
                .countDistinctProjetByInvestisseurIdAndStatutPaiement(
                        userId, StatutPaiement.CONFIRME);

        long nombreFavoris = favoriRepository.countByUtilisateurId(userId);

        // ── Graphe ligne — Evolution mensuelle ────────────────

        List<EvolutionMensuelle> evolutionMensuelle = investissementRepository
                .findEvolutionMensuelle(userId, StatutPaiement.CONFIRME)
                .stream()
                .map(row -> EvolutionMensuelle.builder()
                        .mois(getMonthName((Integer) row[0]) + " " + row[1])
                        .montantInvesti((BigDecimal) row[2])
                        .nombreInvestissements(((Number) row[3]).intValue())
                        .build())
                .collect(Collectors.toList());

        // ── Graphe donut — Répartition par projet ─────────────

        List<RepartitionProjet> repartitionProjets = investissementRepository
                .findRepartitionParProjet(userId, StatutPaiement.CONFIRME)
                .stream()
                .map(row -> {
                    BigDecimal montant = (BigDecimal) row[1];
                    double pourcentage = totalInvesti.compareTo(BigDecimal.ZERO) > 0
                            ? montant.divide(totalInvesti, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue()
                            : 0.0;
                    return RepartitionProjet.builder()
                            .projetId((UUID) row[0])
                            .projetTitre((String) row[2])
                            .montant(montant)
                            .pourcentage(pourcentage)
                            .build();
                })
                .collect(Collectors.toList());

        // ── Tableau — 5 derniers investissements ──────────────

        List<InvestissementResume> derniersInvestissements =
                investissementRepository
                        .findTop5ByInvestisseurIdAndStatutPaiementOrderByCreatedAtDesc(
                                userId, StatutPaiement.CONFIRME,
                                PageRequest.of(0, 5))
                        .stream()
                        .map(this::toInvestissementResume)
                        .collect(Collectors.toList());

        // ── Section favoris — 5 derniers ──────────────────────

        List<FavoriResume> derniersFavoris = favoriRepository
                .findTop5ByUtilisateurIdOrderByCreatedAtDesc(
                        userId, PageRequest.of(0, 5))
                .stream()
                .map(this::toFavoriResume)
                .collect(Collectors.toList());

        log.info("Dashboard INVESTOR construit — totalInvesti: {}, " +
                        "nombreInvestissements: {}, nombreFavoris: {}",
                totalInvesti, nombreInvestissements, nombreFavoris);

        return UserDashboardResponse.builder()
                .role("INVESTOR")
                .kycValide(kycValide)
                .notificationsNonLues(notificationsNonLues)
                .totalInvesti(totalInvesti)
                .nombreInvestissements(nombreInvestissements)
                .nombreProjetsFinances(nombreProjetsFinances)
                .nombreFavoris(nombreFavoris)
                .evolutionMensuelle(evolutionMensuelle)
                .repartitionProjets(repartitionProjets)
                .derniersInvestissements(derniersInvestissements)
                .derniersFavoris(derniersFavoris)
                .build();
    }

    // ================================================================
    // DASHBOARD CREATEUR DE PROJET
    // ================================================================

    private UserDashboardResponse buildCreatorDashboard(
            UUID userId,
            boolean kycValide,
            long notificationsNonLues) {

        log.info("Construction dashboard PROJECT_CREATOR pour userId: {}", userId);

        long nombreProjets = projetRepository.countByPorteurId(userId);

        long projetsEnAttente = projetRepository
                .countByPorteurIdAndStatut(userId, StatutProjet.EN_ATTENTE);

        long projetsValides = projetRepository
                .countByPorteurIdAndStatut(userId, StatutProjet.VALIDE);

        // ✅ Null safety
        BigDecimal montantTotalCollecte = Optional.ofNullable(
                        projetRepository.sumMontantActuelByPorteurId(userId))
                .orElse(BigDecimal.ZERO);

        List<ProjetResume> derniersProjets = projetRepository
                .findTop5ByPorteurIdOrderByCreatedAtDesc(
                        userId, PageRequest.of(0, 5))
                .stream()
                .map(this::toProjetResume)
                .collect(Collectors.toList());

        log.info("Dashboard PROJECT_CREATOR construit — nombreProjets: {}, " +
                "montantTotalCollecte: {}", nombreProjets, montantTotalCollecte);

        return UserDashboardResponse.builder()
                .role("PROJECT_CREATOR")
                .kycValide(kycValide)
                .notificationsNonLues(notificationsNonLues)
                .nombreProjets(nombreProjets)
                .projetsEnAttente(projetsEnAttente)
                .projetsValides(projetsValides)
                .montantTotalCollecte(montantTotalCollecte)
                .derniersProjets(derniersProjets)
                .build();
    }

    // ================================================================
    // MAPPERS PRIVES
    // ================================================================

    private InvestissementResume toInvestissementResume(Investissement inv) {
        return InvestissementResume.builder()
                .projetId(inv.getProjet().getId())
                .projetTitre(inv.getProjet().getTitre())
                .montant(inv.getMontant())
                .dateInvestissement(inv.getCreatedAt())
                .statut(inv.getStatutPaiement().name())
                .build();
    }

    private ProjetResume toProjetResume(Projet p) {
        return ProjetResume.builder()
                .projetId(p.getId())
                .titre(p.getTitre())
                .statut(p.getStatut().name())
                .montantActuel(p.getMontantActuel())
                .dateFin(p.getDateFin())
                .build();
    }

    private FavoriResume toFavoriResume(Favori favori) {
        Projet p = favori.getProjet();

        // Calcul pourcentage financement
        double pourcentage = p.getObjectifFinancier() != null
                && p.getObjectifFinancier().compareTo(BigDecimal.ZERO) > 0
                ? p.getMontantActuel()
                .divide(p.getObjectifFinancier(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
                : 0.0;

        return FavoriResume.builder()
                .projetId(p.getId())
                .projetTitre(p.getTitre())
                .montantObjectif(p.getObjectifFinancier())
                .montantActuel(p.getMontantActuel())
                .pourcentageFinancement(pourcentage)
                .statut(p.getStatut().name())
                .build();
    }

    // ================================================================
    // HELPERS
    // ================================================================

    private String getMonthName(int mois) {
        return switch (mois) {
            case 1  -> "Jan";
            case 2  -> "Fev";
            case 3  -> "Mar";
            case 4  -> "Avr";
            case 5  -> "Mai";
            case 6  -> "Jun";
            case 7  -> "Jul";
            case 8  -> "Aou";
            case 9  -> "Sep";
            case 10 -> "Oct";
            case 11 -> "Nov";
            case 12 -> "Dec";
            default -> "?";
        };
    }
}

