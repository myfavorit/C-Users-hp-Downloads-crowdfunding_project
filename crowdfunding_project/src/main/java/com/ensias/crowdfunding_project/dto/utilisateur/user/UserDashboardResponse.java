package com.ensias.crowdfunding_project.dto.utilisateur.user;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class UserDashboardResponse {

    // ── Commun aux deux rôles ──────────────────────────────────
    private String  role;
    private boolean kycValide;
    private long    notificationsNonLues;

    // ── INVESTOR uniquement ────────────────────────────────────
    private BigDecimal totalInvesti;
    private long       nombreInvestissements;
    private long       nombreFavoris;
    private long       nombreProjetsFinances;

    /** Graphe ligne : évolution mensuelle des investissements */
    private List<EvolutionMensuelle>   evolutionMensuelle;

    /** Graphe donut : répartition des montants par projet */
    private List<RepartitionProjet>    repartitionProjets;

    /** Tableau : 5 derniers investissements effectués */
    private List<InvestissementResume> derniersInvestissements;

    /** Section favoris : 5 derniers projets mis en favori */
    private List<FavoriResume>         derniersFavoris;

    // ── PROJECT_CREATOR uniquement ─────────────────────────────
    private long       nombreProjets;
    private long       projetsEnAttente;
    private long       projetsValides;
    private BigDecimal montantTotalCollecte;

    /** Tableau : 5 derniers projets créés */
    private List<ProjetResume>         derniersProjets;

    /** Graphe ligne : évolution mensuelle des montants collectés */
    private List<EvolutionCollecte>    evolutionCollecte;

    /** Graphe donut : répartition des projets par statut */
    private List<RepartitionStatut>    repartitionStatuts;

    /** Tableau : 5 derniers investissements reçus sur ses projets */
    private List<InvestissementRecu>   derniersInvestissementsRecus;
}