package com.ensias.crowdfunding_project.repositories;

import com.ensias.crowdfunding_project.entities.Notification;
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

    // ── Recherche par Destinataire ──────────────────────────────

    // Toutes les notifications d'un utilisateur triées par les plus récentes
    List<Notification> findByDestinataireIdOrderByCreatedAtDesc(UUID destinataireId);

    // Notifications filtrées par statut de lecture (lu/non lu)
    List<Notification> findByDestinataireIdAndLuOrderByCreatedAtDesc(UUID destinataireId, Boolean lu);

    // ── Requêtes Spécifiques (JPQL) ──────────────────────────────

    // Récupérer uniquement les notifications non lues d'un utilisateur
    @Query("SELECT n FROM Notification n " +
            "WHERE n.destinataire.id = :destinataireId " +
            "AND n.lu = false " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByDestinataireId(@Param("destinataireId") UUID destinataireId);

    // Récupérer les notifications par type (ex: 'INVESTISSEMENT', 'SYSTEME')
    @Query("SELECT n FROM Notification n " +
            "WHERE n.destinataire.id = :destinataireId " +
            "AND n.type = :type " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findByTypeAndDestinataireId(
            @Param("destinataireId") UUID destinataireId,
            @Param("type") String type
    );

    // ── Actions de Mise à Jour (Bulk) ───────────────────────────

    // Marquer toutes les notifications d'un utilisateur comme lues
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.lu = true " +
            "WHERE n.destinataire.id = :destinataireId " +
            "AND n.lu = false")
    int markAllAsRead(@Param("destinataireId") UUID destinataireId);

    // Marquer une notification spécifique comme lue
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.lu = true " +
            "WHERE n.id = :notificationId")
    void markAsRead(@Param("notificationId") UUID notificationId);

    // ── Suppression ──────────────────────────────────────────────

    // Supprimer les notifications très anciennes (ménage de la base)
    @Modifying
    @Transactional
    void deleteByDestinataireId(UUID destinataireId);

    // ── Statistiques ─────────────────────────────────────────────

    // Compter le nombre de notifications non lues (pour afficher la pastille rouge sur le profil)
    long countByDestinataireIdAndLuFalse(UUID destinataireId);

    // Compter le total des notifications d'un utilisateur
    long countByDestinataireId(UUID destinataireId);
}