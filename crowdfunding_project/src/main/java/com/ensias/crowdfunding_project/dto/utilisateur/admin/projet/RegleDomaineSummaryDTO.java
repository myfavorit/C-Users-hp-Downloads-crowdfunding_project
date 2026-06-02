package com.ensias.crowdfunding_project.dto.utilisateur.admin.projet;

import com.ensias.crowdfunding_project.enums.DomaineProjet;
import com.ensias.crowdfunding_project.enums.RegleDomaine;

import java.math.BigDecimal;

// Résumé des règles du domaine (informatif dans ProjetDetailDTO)
public record RegleDomaineSummaryDTO(
        DomaineProjet domaine,
        int dureeMaxJours,
        BigDecimal objectifMax
) {
    public static RegleDomaineSummaryDTO from(RegleDomaine r, DomaineProjet d) {
        return new RegleDomaineSummaryDTO(d, r.getDureeMaxJours(), r.getObjectifMax());
    }
}
