package com.ensias.crowdfunding_project.dto.utilisateur.admin.projet;

// ============================================================
// 4. DTOs — SOUS-OBJETS (imbriqués dans les réponses)
// ============================================================

import java.time.LocalDateTime;

// Résumé du porteur de projet
public record PorteurSummaryDTO(
        Long id,
        String nom,
        String prenom,
        String email,
        LocalDateTime dateInscription
) {}
