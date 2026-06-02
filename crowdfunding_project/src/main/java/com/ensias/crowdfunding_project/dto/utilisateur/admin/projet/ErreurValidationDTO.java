package com.ensias.crowdfunding_project.dto.utilisateur.admin.projet;

import java.util.List;
import java.util.Map;

// Réponse erreur structurée
public record ErreurValidationDTO(
        String erreur,
        List<String> details,        // violations métier
        Map<String, List<String>> contenuInterdit   // null si pas de problème contenu
) {}
