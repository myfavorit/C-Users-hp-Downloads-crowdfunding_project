package com.ensias.crowdfunding_project.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CreatorDashboardResponse {

    // ── Stats cards ───────────────────────────────────────────────────────
    private long totalProjets;
    private long projetsEnAttente;
    private long projetsValides;
    private BigDecimal montantCollecte;
    private long nombreInvestisseurs;
    private boolean kycValide;

    // ── Donut chart ───────────────────────────────────────────────────────
    private StatutBreakdown statutBreakdown;

    // ── Top 3 projects table ──────────────────────────────────────────────
    private List<ProjetSummary> topProjets;

    // ── Last 5 investments received table ────────────────────────────────
    private List<InvestissementRecuSummary> derniersInvestissementsRecus;

    // ── Inner classes ─────────────────────────────────────────────────────

    @Getter
    @Builder
    public static class ProjetSummary {
        private UUID projetId;
        private String titre;
        private BigDecimal montantActuel;
        private BigDecimal objectifFinancier;
        private BigDecimal pourcentageFinancement;
        private String statut;
    }

    @Getter
    @Builder
    public static class InvestissementRecuSummary {
        private String investisseurNom;
        private String investisseurPrenom;
        private String projetTitre;
        private BigDecimal montant;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    public static class StatutBreakdown {
        private long enAttente;
        private long valides;
        private long rejetes;
        private long clotures;
    }
}