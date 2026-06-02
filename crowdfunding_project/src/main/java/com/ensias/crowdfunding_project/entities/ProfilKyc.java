package com.ensias.crowdfunding_project.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "profil_kyc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfilKyc {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    private Utilisateur utilisateur;

    @Column(name = "photo_profil", length = 255)
    private String photoProfil;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false, length = 255)
    private String rib;

    @Column(name = "kyc_valide", nullable = false)
    @Builder.Default
    private boolean kycValide = false;

    @Column(name = "kyc_soumis_at")
    private LocalDateTime kycSoumisAt;

    @Column(name = "kyc_valide_at")
    private LocalDateTime kycValideAt;

    @Column(columnDefinition = "TEXT")
    private String motifRejet;

    @Column(name = "date_traitement")
    private LocalDateTime dateTraitement;

    @PrePersist
    protected void onCreate() {
        if (kycSoumisAt == null) {
            kycSoumisAt = LocalDateTime.now();
        }
    }

    public void valider() {
        if (kycValide) {
            throw new IllegalStateException("Le KYC est déjà validé");
        }
        this.kycValide = true;
        this.kycValideAt = LocalDateTime.now();
    }

    public void rejeter() {
        this.kycValide = false;
        this.kycValideAt = null;
        this.kycSoumisAt = null;   // L'utilisateur devra resoumettre
    }

    public boolean estEnAttente() {
        return !kycValide && kycSoumisAt != null;
    }


}
