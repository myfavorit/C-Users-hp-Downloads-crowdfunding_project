package com.ensias.crowdfunding_project.dto.commentaire;

import com.ensias.crowdfunding_project.entities.Commentaires;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CommentaireResponse {

    private UUID id;
    private UUID projetId;
    private UUID auteurId;
    private String auteurNom;
    private String auteurPrenom;
    private String contenu;
    private LocalDateTime createdAt;

    // Converts entity → DTO
    public static CommentaireResponse from(Commentaires commentaire) {
        return CommentaireResponse.builder()
                .id(commentaire.getId())
                .projetId(commentaire.getProjet().getId())
                .auteurId(commentaire.getAuteur().getId())
                .auteurNom(commentaire.getAuteur().getNom())
                .auteurPrenom(commentaire.getAuteur().getPrenom())
                .contenu(commentaire.getContenu())
                .createdAt(commentaire.getCreatedAt())
                .build();
    }
}