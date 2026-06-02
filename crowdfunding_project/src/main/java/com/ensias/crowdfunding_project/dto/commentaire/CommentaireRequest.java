package com.ensias.crowdfunding_project.dto.commentaire;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CommentaireRequest {

    @NotNull(message = "L'id du projet est obligatoire")
    private UUID projetId;

    @NotBlank(message = "Le contenu ne peut pas être vide")
    @Size(max = 2000, message = "Le contenu ne peut pas dépasser 2000 caractères")
    private String contenu;
}