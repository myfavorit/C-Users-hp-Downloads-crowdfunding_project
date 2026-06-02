package com.ensias.crowdfunding_project.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @NotBlank(message = "Le token est obligatoire")
    private String token;
}