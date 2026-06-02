package com.ensias.crowdfunding_project.dto.utilisateur.admin.projet;

// ============================================================
// 1. DTOs — ENTRÉE (Requêtes)
// ============================================================

import com.ensias.crowdfunding_project.enums.DomaineProjet;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

// Soumission d'un nouveau projet par le porteur
public record ProjetCreationDTO(

        @NotBlank(message = "Le titre est obligatoire")
        @Size(min = 5, max = 100)
        String titre,

        @NotBlank(message = "La description est obligatoire")
        @Size(min = 50, max = 2000)
        String description,

        @NotNull(message = "Le domaine est obligatoire")
        DomaineProjet domaine,

        @NotNull
        @DecimalMin(value = "100.0", message = "Objectif minimum : 100 €")
        BigDecimal objectifFinancement,

        @Min(value = 1, message = "Durée minimum : 1 jour")
        int dureeJours,

        @NotBlank
        String localisation,

        // Optionnel : URL image de couverture
        String imageUrl
) {}
