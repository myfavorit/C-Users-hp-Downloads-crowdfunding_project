package com.ensias.crowdfunding_project.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    private UUID userId;      // ← ajouter
    private String token;
    private String email;
    private String nom;
    private String prenom;
    private String role;
    private String statut;    // ← ajouter
    private boolean kycValide; // ← ajouter
    private long expiresIn;

}