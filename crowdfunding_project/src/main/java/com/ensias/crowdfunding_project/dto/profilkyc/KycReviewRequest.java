package com.ensias.crowdfunding_project.dto.profilkyc;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KycReviewRequest {
    @NotBlank(message = "La décision est obligatoire (APPROUVE ou REJETE)")
    private String decision;
    private String motif;   // obligatoire si decision = REJETE
}