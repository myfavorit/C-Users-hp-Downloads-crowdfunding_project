package com.ensias.crowdfunding_project.repositories;

import com.ensias.crowdfunding_project.entities.ProfilKyc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfilKycRepository extends JpaRepository<ProfilKyc, UUID> {

    Optional<ProfilKyc> findByUtilisateurId(UUID utilisateurId);
    boolean existsByUtilisateurId(UUID utilisateurId);

    Optional<ProfilKyc> findByUtilisateurIdAndKycValide(
            UUID utilisateurId, Boolean kycValide
    );

    // KYC soumis mais pas encore validés — dashboard admin
    List<ProfilKyc> findByKycValideFalseAndKycSoumisAtIsNotNull();

    @Modifying
    @Transactional
    @Query("UPDATE ProfilKyc p SET p.kycValide = true, " +
            "p.kycValideAt = :now WHERE p.utilisateur.id = :utilisateurId")
    int validerKyc(
            @Param("utilisateurId") UUID utilisateurId,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Transactional
    @Query("UPDATE ProfilKyc p SET p.kycSoumisAt = :now " +
            "WHERE p.utilisateur.id = :utilisateurId")
    int soumettreKyc(
            @Param("utilisateurId") UUID utilisateurId,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT COUNT(p) FROM ProfilKyc p " +
            "WHERE p.kycValide = false AND p.kycSoumisAt IS NOT NULL")
    long countKycEnAttente();
}