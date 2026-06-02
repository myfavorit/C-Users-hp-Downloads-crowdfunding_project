package com.ensias.crowdfunding_project.dto.utilisateur.admin.projet;

import com.ensias.crowdfunding_project.enums.DomaineProjet;

import java.math.BigDecimal;

// Détail d'une violation (filtre 2)
public record ViolationDTO(

        String champ,       // "dureeJours" ou "objectifFinancement"
        String message,     // message lisible
        Object valeurSoumise,
        Object valeurMax
) {
    // Fabrique statique pour la durée
    public static ViolationDTO duree(int soumis, int max, DomaineProjet domaine) {
        return new ViolationDTO(
                "dureeJours",
                "Durée %d j dépasse le max autorisé (%d j) pour %s".formatted(soumis, max, domaine),
                soumis, max
        );
    }

    // Fabrique statique pour l'objectif
    public static ViolationDTO objectif(BigDecimal soumis, BigDecimal max, DomaineProjet domaine) {
        return new ViolationDTO(
                "objectifFinancement",
                "Objectif %s € dépasse le max autorisé (%s €) pour %s".formatted(soumis, max, domaine),
                soumis, max
        );
    }
}
