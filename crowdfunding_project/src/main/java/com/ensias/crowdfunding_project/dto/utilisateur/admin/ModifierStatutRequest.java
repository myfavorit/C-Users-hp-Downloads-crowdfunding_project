package com.ensias.crowdfunding_project.dto.utilisateur.admin;

import com.ensias.crowdfunding_project.enums.StatutCompte;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ModifierStatutRequest {
    @NotNull
    private StatutCompte statut;
    private String motif;  // obligatoire si statut = SUSPENDU ou BANNI
}