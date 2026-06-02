package com.ensias.crowdfunding_project.dto.utilisateur.admin.projet;

// ============================================================
// 3. DTOs — RAPPORT DE VALIDATION (Résultat des deux filtres)
// ============================================================

import com.ensias.crowdfunding_project.enums.DomaineProjet;

// Rapport complet renvoyé à l'admin après analyse
public record RapportValidationDTO(

        Long projetId,
        String titre,
        DomaineProjet domaine,

        boolean estValide,               // true seulement si les 2 filtres passent

        // ── Filtre 1 : Modération contenu ──
        ModerationResultatDTO moderationContenu,

        // ── Filtre 2 : Règles métier domaine ──
        ReglesMetierResultatDTO reglesMetier,

        String resume                    // message synthétique pour l'admin
) {}
