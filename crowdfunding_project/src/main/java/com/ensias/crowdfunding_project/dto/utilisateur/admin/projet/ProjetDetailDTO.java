package com.ensias.crowdfunding_project.dto.utilisateur.admin.projet;

import com.ensias.crowdfunding_project.enums.DomaineProjet;
import com.ensias.crowdfunding_project.enums.StatutProjet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Détail complet d'un projet (vue admin ou vue publique)
public record ProjetDetailDTO(

        Long id,
        String titre,
        String description,
        DomaineProjet domaine,
        StatutProjet statut,

        BigDecimal objectifFinancement,
        BigDecimal montantCollecte,      // calculé dynamiquement
        int pourcentageAtteint,          // calculé : montantCollecte / objectif * 100

        int dureeJours,
        LocalDate dateDebut,
        LocalDate dateFin,               // calculé : dateDebut + dureeJours

        String localisation,
        String imageUrl,

        LocalDateTime dateSoumission,
        LocalDateTime dateDecision,      // null si EN_ATTENTE
        String motifRefus,               // null si APPROUVÉ

        // Règles du domaine associées (informatif)
        RegleDomaineSummaryDTO reglesDomaine,

        // Porteur
        PorteurSummaryDTO porteur
) {}