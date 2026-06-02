package com.ensias.crowdfunding_project.dto.utilisateur.user;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProjetResume {
    private UUID projetId;
    private String titre;
    private String statut;
    private BigDecimal montantActuel;
    private LocalDate dateFin;
}