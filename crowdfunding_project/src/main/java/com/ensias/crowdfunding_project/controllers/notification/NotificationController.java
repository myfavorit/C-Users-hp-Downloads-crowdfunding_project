package com.ensias.crowdfunding_project.controllers.notification;

import com.ensias.crowdfunding_project.dto.notification.NotificationResponse;
import com.ensias.crowdfunding_project.services.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getMesNotifications(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(notificationService.getMesNotifications(pageable));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> countUnread() {
        long count = notificationService.getNonLuesCount();
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> marquerLue(@PathVariable UUID id) {
        notificationService.marquerCommeLue(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> marquerToutesLues() {
        notificationService.marquerToutesCommeLues();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerNotification(@PathVariable UUID id) {
        notificationService.supprimerNotification(id);
        return ResponseEntity.noContent().build();
    }
}