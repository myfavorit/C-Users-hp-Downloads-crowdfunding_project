package com.ensias.crowdfunding_project.dto.investissement;

import com.ensias.crowdfunding_project.entities.Investissement.ModePaiement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class InvestissementRequest {

    @NotNull(message = "L'id du projet est obligatoire")
    private UUID projetId;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "10.00", message = "Le montant minimum est de 10 MAD")
    private BigDecimal montant;

    // Optional — defaults to SIMULATION if not provided
    private ModePaiement modePaiement;
}