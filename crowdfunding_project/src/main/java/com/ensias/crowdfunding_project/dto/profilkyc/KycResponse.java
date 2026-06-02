package com.ensias.crowdfunding_project.dto.profilkyc;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class KycResponse {
    private UUID id;
    private UUID utilisateurId;
    private String nomUtilisateur;   // ← ajout
    private String statut;
    private String photoProfil;
    private String bio;
    private String rib;
    private boolean kycValide;
    private LocalDateTime kycSoumisAt;
    private LocalDateTime kycValideAt;
}