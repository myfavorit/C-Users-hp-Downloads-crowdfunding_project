package com.ensias.crowdfunding_project.dto.utilisateur.admin.projet;

import com.ensias.crowdfunding_project.services.utilisateur.ModerationService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Résultat du filtre 1 — Modération
public record ModerationResultatDTO(

        boolean estInterdit,
        List<String> motsTrouves,

        // Ex: { "VIOLENCE": ["meurtre"], "TERRORISME": ["attentat"] }
        Map<String, List<String>> parCategorie,

        String resume
) {
    // Fabrique depuis votre ResultatModeration existant
    public static ModerationResultatDTO from(ModerationService.ResultatModeration r) {
        Map<String, List<String>> categories = r.parCategorie().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(),
                        Map.Entry::getValue
                ));
        return new ModerationResultatDTO(
                r.estInterdit(),
                r.motsTrouves(),
                categories,
                r.resume()
        );
    }
}
