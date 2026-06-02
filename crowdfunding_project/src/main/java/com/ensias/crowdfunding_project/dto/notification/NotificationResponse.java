package com.ensias.crowdfunding_project.dto.notification;
import com.ensias.crowdfunding_project.enums.TypeNotification;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID id;
    private String titre;
    private String message;
    private TypeNotification type;
    private boolean lu;
    private LocalDateTime createdAt;
}
