package com.ensias.crowdfunding_project.dto.validation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ValidationRequest {

    @NotNull(message = "L'id du projet est obligatoire")
    private UUID projetId;

    // Only required for rejection — validated in service
    @Size(max = 1000, message = "Le motif ne peut pas dépasser 1000 caractères")
    private String motifRefus;
}