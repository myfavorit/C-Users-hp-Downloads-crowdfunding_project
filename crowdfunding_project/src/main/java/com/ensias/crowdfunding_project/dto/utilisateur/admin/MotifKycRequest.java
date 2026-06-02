package com.ensias.crowdfunding_project.dto.utilisateur.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MotifKycRequest {

    private String entityId;
    private String motif;
}