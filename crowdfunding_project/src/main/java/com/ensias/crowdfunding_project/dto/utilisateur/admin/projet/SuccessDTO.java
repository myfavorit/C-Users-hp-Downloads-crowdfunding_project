package com.ensias.crowdfunding_project.dto.utilisateur.admin.projet;

// ============================================================
// 5. DTOs — RÉPONSES GÉNÉRIQUES
// ============================================================

// Réponse success simple
public record SuccessDTO(
        String statut,       // "APPROUVÉ" | "REFUSÉ" | "SOUMIS"
        Long projetId,
        String message
) {}
