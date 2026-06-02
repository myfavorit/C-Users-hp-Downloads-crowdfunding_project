package com.ensias.crowdfunding_project.dto.projet;

import com.ensias.crowdfunding_project.entities.Projet;
import com.ensias.crowdfunding_project.enums.DomaineProjet;
import com.ensias.crowdfunding_project.enums.StatutProjet;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ProjetResponse {

    private UUID id;

    // Creator info
    private UUID porteurId;
    private String porteurNom;
    private String porteurPrenom;

    // Project info
    private String titre;
    private String description;
    private String imagePrincipale;
    private DomaineProjet domaine;

    // Funding
    private BigDecimal objectifFinancier;
    private BigDecimal montantActuel;
    private BigDecimal montantRestant;          // new ✅
    private BigDecimal pourcentageFinancement;
    private Integer dureeJours;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    // Contrepartie
    private BigDecimal pourcentageOffert;
    private String justificationValuation;

    // Status
    private StatutProjet statut;
    private Boolean isDeleted;
    private Boolean objectifAtteint;            // new ✅

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProjetResponse from(Projet projet) {
        return ProjetResponse.builder()
                .id(projet.getId())
                .porteurId(projet.getPorteur().getId())
                .porteurNom(projet.getPorteur().getNom())
                .porteurPrenom(projet.getPorteur().getPrenom())
                .titre(projet.getTitre())
                .description(projet.getDescription())
                .imagePrincipale(projet.getImagePrincipale())
                .domaine(projet.getDomaine())
                .objectifFinancier(projet.getObjectifFinancier())
                .montantActuel(projet.getMontantActuel())
                .montantRestant(projet.getMontantRestant())
                .pourcentageFinancement(projet.getPourcentageFinancement())
                .dureeJours(projet.getDureeJours())
                .dateDebut(projet.getDateDebut())
                .dateFin(projet.getDateFin())
                .pourcentageOffert(projet.getPourcentageOffert())
                .justificationValuation(projet.getJustificationValuation())
                .statut(projet.getStatut())
                .isDeleted(projet.getIsDeleted())
                .objectifAtteint(projet.isObjectifAtteint())
                .createdAt(projet.getCreatedAt())
                .updatedAt(projet.getUpdatedAt())
                .build();
    }
}