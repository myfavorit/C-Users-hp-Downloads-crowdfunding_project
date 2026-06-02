package com.ensias.crowdfunding_project.dto.utilisateur.user;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class EvolutionMensuelle {
    private String mois;
    private BigDecimal montantInvesti;
    private int nombreInvestissements;
}