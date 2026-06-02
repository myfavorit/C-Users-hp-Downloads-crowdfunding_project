package com.ensias.crowdfunding_project.dto.utilisateur.admin;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String nom;
    private String prenom;
    private String email;
    private String role;
    private String statut;
    private LocalDateTime createdAt;
    private boolean kycValide;
}
