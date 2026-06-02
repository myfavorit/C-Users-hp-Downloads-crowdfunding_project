package com.ensias.crowdfunding_project.dto.utilisateur.admin.projet;

import com.ensias.crowdfunding_project.enums.StatutProjet;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProjetAdminResponse {
    private UUID id;
    private String titre;
    private String description;
    private String domaine;
    private BigDecimal objectifFinancier;
    private BigDecimal montantActuel;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private StatutProjet statut;       // ← enum
    private UUID porteurId;
    private String porteurNom;
    private String porteurPrenom;
    private LocalDateTime createdAt;
}