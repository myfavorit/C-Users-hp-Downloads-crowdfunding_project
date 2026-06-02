package com.ensias.crowdfunding_project.dto.profilkyc;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class KycStatusResponse {
    private boolean kycValide;
    private String statut; // "EN_ATTENTE", "APPROUVE", "REJETE", "NON_SOUMIS"
    private LocalDateTime kycSoumisAt;
    private LocalDateTime kycValideAt;
    private String motifRejet; // si rejeté
}
