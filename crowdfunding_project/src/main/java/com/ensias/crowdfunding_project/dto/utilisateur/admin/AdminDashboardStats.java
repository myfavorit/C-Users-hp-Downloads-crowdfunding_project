package com.ensias.crowdfunding_project.dto.utilisateur.admin;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class AdminDashboardStats {
    // Utilisateurs
    private long totalUtilisateurs;
    private long utilisateursActifs;
    private long utilisateursSuspendus;
    private long utilisateursBannis;

    // Projets
    private long projetsEnAttente;
    private long projetsValides;

    // KYC
    private long kycEnAttente;

    // Financement global
    private BigDecimal montantTotalCollecte;

    // 🆕 INVESTISSEMENTS (ajout)
    private long totalInvestissements;
    private BigDecimal montantTotalInvesti;
    private BigDecimal montantMoyenInvestissement;
    private long totalInvestisseurs;
}