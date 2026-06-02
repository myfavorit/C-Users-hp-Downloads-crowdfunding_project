package com.ensias.crowdfunding_project.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AdminDashboardResponse {

    // ── Users ────────────────────────────────────────────────────────────
    private long totalUsers;
    private long totalInvestors;
    private long totalCreators;
    private long totalAdmins;

    // ── Projects ─────────────────────────────────────────────────────────
    private long totalProjets;
    private long projetsEnAttente;    // needs admin action ⚠️
    private long projetsValides;
    private long projetsRejetes;
    private long projetsClotures;

    // ── Financial ────────────────────────────────────────────────────────
    private BigDecimal montantTotalCollecte;
    private BigDecimal montantTotalInvesti;

    // ── Validation ───────────────────────────────────────────────────────
    private double tauxApprobation;   // percentage

    // ── KYC ──────────────────────────────────────────────────────────────
    private long kycEnAttente;        // needs admin action ⚠️
    private long kycValides;

    // ── Investments ──────────────────────────────────────────────────────
    private long totalInvestissements;
}