package com.ensias.crowdfunding_project.dto.projet;

import com.ensias.crowdfunding_project.enums.DomaineProjet;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProjetRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotNull(message = "Le domaine est obligatoire")
    private DomaineProjet domaine;

    @NotNull(message = "L'objectif financier est obligatoire")
    @DecimalMin(value = "100.00", message = "L'objectif minimum est 100 MAD")
    private BigDecimal objectifFinancier;

    @NotNull(message = "La durée est obligatoire")
    @Min(value = 7, message = "La durée minimum est 7 jours")
    @Max(value = 90, message = "La durée maximum est 90 jours")
    private Integer dureeJours;

    @NotNull(message = "Le pourcentage offert est obligatoire")
    @DecimalMin(value = "1.00", message = "Le pourcentage minimum est 1%")
    @DecimalMax(value = "49.00", message = "Le pourcentage maximum est 49%")
    private BigDecimal pourcentageOffert;

    private String justificationValuation;
    private String imagePrincipale;
}