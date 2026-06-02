package com.ensias.crowdfunding_project.dto.profilkyc;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KycSubmissionRequest {
    private String photoProfil;
    private String bio;
    @NotBlank(message = "Le RIB est obligatoire")
    private String rib;
}