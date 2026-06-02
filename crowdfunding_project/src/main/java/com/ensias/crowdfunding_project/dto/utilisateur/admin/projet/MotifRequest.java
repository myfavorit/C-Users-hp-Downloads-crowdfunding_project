package com.ensias.crowdfunding_project.dto.utilisateur.admin.projet;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MotifRequest {
    @NotBlank
    private String motif;
}