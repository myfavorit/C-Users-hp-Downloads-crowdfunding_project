package com.ensias.crowdfunding_project.dto.media;

import com.ensias.crowdfunding_project.entities.Media.TypeMedia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MediaRequest {

    @NotBlank(message = "L'URL est obligatoire")
    @Size(max = 255, message = "L'URL ne peut pas dépasser 255 caractères")
    private String url;

    @NotNull(message = "Le type de média est obligatoire")
    private TypeMedia typeMedia;
}