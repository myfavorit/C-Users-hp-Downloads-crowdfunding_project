package com.ensias.crowdfunding_project.dto.profilkyc;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class KycAdminListResponse {
    private UUID id;
    private UUID utilisateurId;
    private String nomUtilisateur;
    private String email; // parfois utile
    private LocalDateTime kycSoumisAt;
    private String statut;
}