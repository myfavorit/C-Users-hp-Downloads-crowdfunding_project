package com.ensias.crowdfunding_project.dto.utilisateur.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RepartitionStatut {
    private String statut;
    private long   nombre;
    private double pourcentage;
}