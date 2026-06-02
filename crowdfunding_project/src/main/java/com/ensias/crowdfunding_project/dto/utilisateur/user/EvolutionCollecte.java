package com.ensias.crowdfunding_project.dto.utilisateur.user;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class EvolutionCollecte {
    private String     mois;               // ex: "Jan 2025"
    private BigDecimal montantCollecte;    // total collecté ce mois
    private int        nombreInvestissements;
}