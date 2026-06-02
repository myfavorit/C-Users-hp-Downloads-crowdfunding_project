package com.ensias.crowdfunding_project.services.notification;

import com.ensias.crowdfunding_project.dto.notification.NotificationResponse;
import com.ensias.crowdfunding_project.entities.Notification;
import com.ensias.crowdfunding_project.entities.Utilisateur;
import com.ensias.crowdfunding_project.enums.StatutCompte;
import com.ensias.crowdfunding_project.enums.TypeNotification;
import com.ensias.crowdfunding_project.exception.BusinessException;
import com.ensias.crowdfunding_project.exception.ResourceNotFoundException;
import com.ensias.crowdfunding_project.repositories.NotificationRepository;
import com.ensias.crowdfunding_project.repositories.UtilisateurRepository;
import com.ensias.crowdfunding_project.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;

    // 1. MÉTHODE GÉNÉRIQUE (appelable par tout service)
    /**
     * Crée et enregistre une notification pour un utilisateur.
     * @param destinataireId UUID du destinataire
     * @param titre Titre de la notification
     * @param message Corps du message
     * @param type Type de notification (enum)
     */
    public void envoyerNotification(UUID destinataireId, String titre, String message, TypeNotification type) {
        if (destinataireId == null) {
            throw new BusinessException("L'ID du destinataire ne peut pas être null");
        }
        Utilisateur destinataire = utilisateurRepository.findById(destinataireId)
                .orElseThrow(() -> new ResourceNotFoundException("Destinataire non trouvé"));
        Notification notification = Notification.builder()
                .destinataire(destinataire)
                .titre(titre)
                .message(message)
                .type(type)
                .lu(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
        log.info("Notification envoyée à {} : {}", destinataireId, titre);
    }

    // 2. MÉTHODES POUR L'UTILISATEUR CONNECTÉ

    public Page<NotificationResponse> getMesNotifications(Pageable pageable) {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new BusinessException("Utilisateur non authentifié");
        return notificationRepository.findByDestinataireIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    public long getNonLuesCount() {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (userId == null) return 0;
        return notificationRepository.countByDestinataireIdAndLuFalse(userId);
    }

    public void marquerCommeLue(UUID notificationId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new BusinessException("Utilisateur non authentifié");
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée"));
        if (!notification.getDestinataire().getId().equals(userId))
            throw new BusinessException("Cette notification ne vous appartient pas");
        notification.setLu(true);
        notificationRepository.save(notification);
        log.info("Notification {} marquée comme lue", notificationId);
    }

    public void marquerToutesCommeLues() {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new BusinessException("Utilisateur non authentifié");
        notificationRepository.marquerToutCommeLu(userId);
        log.info("Toutes les notifications marquées comme lues pour {}", userId);
    }

    public void supprimerNotification(UUID notificationId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new BusinessException("Utilisateur non authentifié");
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée"));
        if (!notification.getDestinataire().getId().equals(userId))
            throw new BusinessException("Vous ne pouvez pas supprimer la notification d'un autre utilisateur");
        notificationRepository.delete(notification);
        log.info("Notification {} supprimée", notificationId);
    }

    // 3. MÉTHODES POUR ADMIN (optionnelles, avec userId explicite)
    // À sécuriser avec @PreAuthorize("hasRole('ADMIN')") dans le contrôleur
    public List<NotificationResponse> getNotificationsParUtilisateur(UUID utilisateurId) {
        if (utilisateurId == null) throw new BusinessException("ID utilisateur null");
        return notificationRepository.findByDestinataireIdOrderByCreatedAtDesc(utilisateurId)
                .stream().map(this::toResponse).toList();
    }

    public long countNonLues(UUID utilisateurId) {
        if (utilisateurId == null) throw new BusinessException("ID utilisateur null");
        return notificationRepository.countByDestinataireIdAndLuFalse(utilisateurId);
    }

    public void supprimerToutesNotifications(UUID utilisateurId) {
        if (utilisateurId == null) throw new BusinessException("ID utilisateur null");
        notificationRepository.deleteAllByDestinataireId(utilisateurId);
        log.info("Toutes les notifications supprimées pour {}", utilisateurId);
    }

    // 4. MAPPAGE DTO
    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .titre(notification.getTitre())
                .message(notification.getMessage())
                .type(notification.getType())
                .lu(notification.isLu())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public void notifierChangementStatut(Utilisateur user, StatutCompte nouveauStatut, String motif) {
    }

    public void notifierProjetValide(Utilisateur porteur, String titre) {
    }

    public void notifierProjetRejete(Utilisateur porteur, String titre, String motif) {
    }
}