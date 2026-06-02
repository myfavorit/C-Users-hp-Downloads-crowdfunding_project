package com.ensias.crowdfunding_project.dto.utilisateur.admin.projet;

// ============================================================
// 2. DTOs — SORTIE (Réponses)
// ============================================================

import com.ensias.crowdfunding_project.enums.DomaineProjet;
import com.ensias.crowdfunding_project.enums.StatutProjet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Résumé léger pour la liste EN_ATTENTE (file de modération admin)
public record ProjetResumeModerationDTO(

        Long id,
        String titre,
        DomaineProjet domaine,
        BigDecimal objectifFinancement,
        int dureeJours,
        StatutProjet statut,
        LocalDateTime dateSoumission,

        // Infos du porteur
        String porteurNom,
        String porteurEmail
) {}
