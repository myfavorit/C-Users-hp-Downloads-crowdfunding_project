package com.ensias.crowdfunding_project.dto.utilisateur.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserProfileResponse {
    private UUID id;
    private String nom;
    private String prenom;
    private String email;
    private String role;
    private String statut;
    private boolean kycValide;
    private LocalDateTime createdAt;
}
