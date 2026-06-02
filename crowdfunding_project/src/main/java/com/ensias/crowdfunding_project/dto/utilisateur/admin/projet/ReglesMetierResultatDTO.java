package com.ensias.crowdfunding_project.dto.utilisateur.admin.projet;

import java.math.BigDecimal;
import java.util.List;

// Résultat du filtre 2 — Règles métier
public record ReglesMetierResultatDTO(

        boolean estConforme,
        List<ViolationDTO> violations,   // vide si conforme

        // Rappel des limites du domaine
        int dureeMaxAutorisee,
        BigDecimal objectifMaxAutorise,

        // Valeurs soumises
        int dureeProjet,
        BigDecimal objectifProjet
) {}
