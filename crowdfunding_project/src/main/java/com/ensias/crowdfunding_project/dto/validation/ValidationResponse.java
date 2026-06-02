package com.ensias.crowdfunding_project.dto.validation;

import com.ensias.crowdfunding_project.entities.Validation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ValidationResponse {

    private UUID id;

    // Project info
    private UUID projetId;
    private String projetTitre;

    // Admin info
    private UUID adminId;
    private String adminNom;
    private String adminPrenom;

    // Decision
    private Validation.Decision decision;
    private String motifRefus;
    private LocalDateTime decidedAt;

    // Converts entity → DTO
    public static ValidationResponse from(Validation validation) {
        return ValidationResponse.builder()
                .id(validation.getId())
                .projetId(validation.getProjet().getId())
                .projetTitre(validation.getProjet().getTitre())
                .adminId(validation.getAdmin().getId())
                .adminNom(validation.getAdmin().getNom())
                .adminPrenom(validation.getAdmin().getPrenom())
                .decision(validation.getDecision())
                .motifRefus(validation.getMotifRefus())
                .decidedAt(validation.getDecidedAt())
                .build();
    }
}