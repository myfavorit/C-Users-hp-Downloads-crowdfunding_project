package com.ensias.crowdfunding_project.dto.utilisateur.user;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class InvestissementResume {
    private UUID projetId;
    private String projetTitre;
    private BigDecimal montant;
    private LocalDateTime dateInvestissement;
    private String statut;
}
