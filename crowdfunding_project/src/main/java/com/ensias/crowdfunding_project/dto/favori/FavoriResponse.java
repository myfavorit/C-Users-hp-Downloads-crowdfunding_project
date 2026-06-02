package com.ensias.crowdfunding_project.dto.favori;

import com.ensias.crowdfunding_project.entities.Favori;
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
public class FavoriResponse {

    private UUID id;

    // User info
    private UUID utilisateurId;

    // Project info (embedded — no need for separate ProjetResponse here)
    private UUID projetId;
    private String projetTitre;
    private String projetDescription;
    private String projetImagePrincipale;
    private DomaineProjet projetDomaine;
    private BigDecimal projetObjectifFinancier;
    private BigDecimal projetMontantActuel;
    private BigDecimal projetPourcentageFinancement;
    private BigDecimal projetPourcentageOffert;
    private StatutProjet projetStatut;
    private LocalDate projetDateFin;

    // Creator info
    private String porteurNom;
    private String porteurPrenom;

    // When added to favorites
    private LocalDateTime createdAt;

    // Converts entity → DTO
    public static FavoriResponse from(Favori favori) {
        return FavoriResponse.builder()
                .id(favori.getId())
                .utilisateurId(favori.getUtilisateur().getId())
                .projetId(favori.getProjet().getId())
                .projetTitre(favori.getProjet().getTitre())
                .projetDescription(favori.getProjet().getDescription())
                .projetImagePrincipale(favori.getProjet().getImagePrincipale())
                .projetDomaine(favori.getProjet().getDomaine())
                .projetObjectifFinancier(favori.getProjet().getObjectifFinancier())
                .projetMontantActuel(favori.getProjet().getMontantActuel())
                .projetPourcentageFinancement(favori.getProjet().getPourcentageFinancement())
                .projetPourcentageOffert(favori.getProjet().getPourcentageOffert())
                .projetStatut(favori.getProjet().getStatut())
                .projetDateFin(favori.getProjet().getDateFin())
                .porteurNom(favori.getProjet().getPorteur().getNom())
                .porteurPrenom(favori.getProjet().getPorteur().getPrenom())
                .createdAt(favori.getCreatedAt())
                .build();
    }
}