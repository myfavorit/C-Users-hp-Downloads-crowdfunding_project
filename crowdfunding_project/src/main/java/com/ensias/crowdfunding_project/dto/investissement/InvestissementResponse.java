package com.ensias.crowdfunding_project.dto.investissement;

import com.ensias.crowdfunding_project.entities.Investissement;
import com.ensias.crowdfunding_project.enums.StatutPaiement;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class InvestissementResponse {

    private UUID id;

    // Investor info
    private UUID investisseurId;
    private String investisseurNom;
    private String investisseurPrenom;

    // Project info
    private UUID projetId;
    private String projetTitre;

    // Investment info
    private BigDecimal montant;

    // Calculated on the fly:
    // (montant / objectifFinancier) * pourcentageOffert
    private BigDecimal partInvestisseur;

    // Payment info
    private StatutPaiement statutPaiement;
    private String referencePaiement;
    private Investissement.ModePaiement modePaiement;

    // Audit
    private LocalDateTime createdAt;

    /**
     * Convert entity to DTO
     * pourcentageOffert and objectifFinancier come from the project
     * to calculate the investor's equity share on the fly
     */
    public static InvestissementResponse from(
            Investissement investissement,
            BigDecimal pourcentageOffert,
            BigDecimal objectifFinancier) {

        // Formula: (montant / objectifFinancier) * pourcentageOffert
        BigDecimal partInvestisseur = BigDecimal.ZERO;
        if (pourcentageOffert != null
                && objectifFinancier != null
                && objectifFinancier.compareTo(BigDecimal.ZERO) > 0) {

            partInvestisseur = investissement.getMontant()
                    .divide(objectifFinancier, 10, RoundingMode.HALF_UP)
                    .multiply(pourcentageOffert)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return InvestissementResponse.builder()
                .id(investissement.getId())
                .investisseurId(investissement.getInvestisseur().getId())
                .investisseurNom(investissement.getInvestisseur().getNom())
                .investisseurPrenom(investissement.getInvestisseur().getPrenom())
                .projetId(investissement.getProjet().getId())
                .projetTitre(investissement.getProjet().getTitre())
                .montant(investissement.getMontant())
                .partInvestisseur(partInvestisseur)
                .statutPaiement(investissement.getStatutPaiement())
                .referencePaiement(investissement.getReferencePaiement())
                .modePaiement(investissement.getModePaiement())
                .createdAt(investissement.getCreatedAt())
                .build();
    }
}

