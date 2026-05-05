package com.ensias.crowdfunding_project.repositories;

import com.ensias.crowdfunding_project.entities.Notification;
import com.ensias.crowdfunding_project.entities.Notification.TypeNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // ============================================================
    // 1. RECHERCHES PAR DESTINATAIRE
    // ============================================================

    /**
     * Toutes les notifications d'un utilisateur (triées du plus récent au plus ancien)
     */
    List<Notification> findByDestinataireIdOrderByCreatedAtDesc(UUID destinataireId);

    /**
     * Notifications d'un utilisateur avec pagination (pour éviter de tout charger)
     */
    Page<Notification> findByDestinataireIdOrderByCreatedAtDesc(UUID destinataireId, Pageable pageable);

    /**
     * Notifications filtrées par statut de lecture
     */
    List<Notification> findByDestinataireIdAndLuOrderByCreatedAtDesc(UUID destinataireId, boolean lu);

    /**
     * Notifications non lues d'un utilisateur
     */
    List<Notification> findByDestinataireIdAndLuFalseOrderByCreatedAtDesc(UUID destinataireId);

    // ============================================================
    // 2. RECHERCHES PAR TYPE
    // ============================================================

    /**
     * Notifications d'un utilisateur par type
     */
    List<Notification> findByDestinataireIdAndTypeOrderByCreatedAtDesc(UUID destinataireId, TypeNotification type);

    // ============================================================
    // 3. MISES À JOUR
    // ============================================================

    /**
     * Marquer toutes les notifications d'un utilisateur comme lues
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.lu = true WHERE n.destinataire.id = :destinataireId AND n.lu = false")
    int marquerToutCommeLu(@Param("destinataireId") UUID destinataireId);

    /**
     * Marquer une notification spécifique comme lue
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.lu = true WHERE n.id = :notificationId")
    int marquerCommeLue(@Param("notificationId") UUID notificationId);

    // ============================================================
    // 4. SUPPRESSION
    // ============================================================

    /**
     * Supprimer toutes les notifications d'un utilisateur (quand compte supprimé)
     */
    @Modifying
    @Transactional
    void deleteByDestinataireId(UUID destinataireId);

    /**
     * Supprimer les notifications plus anciennes que X jours (job programmé)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.createdAt < :date")
    int deleteOldNotifications(@Param("date") java.time.LocalDateTime date);

    // ============================================================
    // 5. STATISTIQUES
    // ============================================================

    /**
     * Compter les notifications non lues d'un utilisateur (pastille rouge)
     */
    long countByDestinataireIdAndLuFalse(UUID destinataireId);

    /**
     * Compter le total des notifications d'un utilisateur
     */
    long countByDestinataireId(UUID destinataireId);
}