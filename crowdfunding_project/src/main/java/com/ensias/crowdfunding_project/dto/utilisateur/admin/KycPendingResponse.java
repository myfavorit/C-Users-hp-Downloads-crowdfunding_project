package com.ensias.crowdfunding_project.dto.utilisateur.admin;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class KycPendingResponse {
    private UUID kycId;
    private String utilisateurNom;
    private String utilisateurPrenom;
    private String utilisateurEmail;
    private LocalDateTime kycSoumisAt;
    // Optionnel : lien vers l'utilisateur
}