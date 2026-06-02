package com.ensias.crowdfunding_project.repositories;

import com.ensias.crowdfunding_project.entities.ProfilKyc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfilKycRepository extends JpaRepository<ProfilKyc, UUID> {

    Optional<ProfilKyc> findByUtilisateurId(UUID utilisateurId);

    // Dossiers en attente (soumis mais non validés)
    @Query("SELECT p FROM ProfilKyc p WHERE p.kycValide = false AND p.kycSoumisAt IS NOT NULL")
    List<ProfilKyc> findPendingKyc();

    // Avec fetch de l'utilisateur pour éviter N+1
    @Query("SELECT p FROM ProfilKyc p JOIN FETCH p.utilisateur WHERE p.kycValide = false AND p.kycSoumisAt IS NOT NULL")
    List<ProfilKyc> findPendingKycWithUtilisateur();

    boolean existsByUtilisateurId(UUID utilisateurId);

    // ✅ AJOUT : Compter les KYC en attente (pour le dashboard admin)
    long countByKycValideFalseAndKycSoumisAtIsNotNull();
    // Derniers KYC en attente (soumis mais non validés) triés par date de soumission
    Page<ProfilKyc> findByKycValideFalseAndKycSoumisAtIsNotNullOrderByKycSoumisAtDesc(Pageable pageable);
}