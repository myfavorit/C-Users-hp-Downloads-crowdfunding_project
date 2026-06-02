package com.ensias.crowdfunding_project.dto.utilisateur.admin.projet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Motif de refus manuel par l'admin
public record MotifRefusDTO(

        @NotBlank(message = "Le motif est obligatoire")
        @Size(min = 10, max = 500, message = "Motif entre 10 et 500 caractères")
        String motif
) {}