package com.ensias.crowdfunding_project.dto.utilisateur.user;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    private String nom;
    private String prenom;
    @Email
    private String email;
    private String ancienMotDePasse;
    private String nouveauMotDePasse;

}