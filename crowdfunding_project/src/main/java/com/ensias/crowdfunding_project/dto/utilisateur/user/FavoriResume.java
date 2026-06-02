package com.ensias.crowdfunding_project.dto.utilisateur.user;

import com.ensias.crowdfunding_project.enums.StatutProjet;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class FavoriResume {
    private UUID projetId;
    private String projetTitre;
    private BigDecimal montantObjectif;
    private BigDecimal montantActuel;
    private double pourcentageFinancement;
    private String statut;
}