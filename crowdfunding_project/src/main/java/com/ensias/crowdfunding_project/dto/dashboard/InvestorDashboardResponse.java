package com.ensias.crowdfunding_project.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class InvestorDashboardResponse {

    // ── Stats cards ───────────────────────────────────────────────────────
    private BigDecimal totalInvesti;
    private long nombreInvestissements;
    private long projetsFinances;
    private BigDecimal partMoyenne;      // average equity share
    private long nombreFavoris;
    private boolean kycValide;

    // ── Last 5 investments table ──────────────────────────────────────────
    private List<InvestissementSummary> dernierInvestissements;

    // ── Top 3 favorites table ─────────────────────────────────────────────
    private List<FavoriSummary> mesFavoris;

    // ── Inner classes ─────────────────────────────────────────────────────

    @Getter
    @Builder
    public static class InvestissementSummary {
        private UUID projetId;
        private String projetTitre;
        private BigDecimal montant;
        private String statutPaiement;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    public static class FavoriSummary {
        private UUID projetId;
        private String projetTitre;
        private BigDecimal pourcentageFinancement;
        private BigDecimal montantActuel;
        private BigDecimal objectifFinancier;
    }
}