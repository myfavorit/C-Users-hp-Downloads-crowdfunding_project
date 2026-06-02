package com.ensias.crowdfunding_project.dto.media;

import com.ensias.crowdfunding_project.entities.Media;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class MediaResponse {

    private UUID id;

    // Project info
    private UUID projetId;
    private String projetTitre;

    // Media info
    private String url;
    private Media.TypeMedia typeMedia;

    // Audit
    private LocalDateTime createdAt;

    // Converts entity → DTO
    public static MediaResponse from(Media media) {
        return MediaResponse.builder()
                .id(media.getId())
                .projetId(media.getProjet().getId())
                .projetTitre(media.getProjet().getTitre())
                .url(media.getUrl())
                .typeMedia(media.getTypeMedia())
                .createdAt(media.getCreatedAt())
                .build();
    }
}