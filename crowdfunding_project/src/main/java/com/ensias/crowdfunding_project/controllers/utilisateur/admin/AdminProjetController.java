package com.ensias.crowdfunding_project.controllers.utilisateur.admin;

import com.ensias.crowdfunding_project.dto.utilisateur.admin.projet.ModerationResultatDTO;
import com.ensias.crowdfunding_project.dto.utilisateur.admin.projet.RapportValidationDTO;
import com.ensias.crowdfunding_project.dto.utilisateur.admin.projet.ReglesMetierResultatDTO;
import com.ensias.crowdfunding_project.dto.utilisateur.admin.projet.ViolationDTO;
import com.ensias.crowdfunding_project.entities.Projet;
import com.ensias.crowdfunding_project.enums.RegleDomaine;
import com.ensias.crowdfunding_project.services.utilisateur.AdminService;
import com.ensias.crowdfunding_project.services.utilisateur.ModerationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/projets")
@RequiredArgsConstructor
@Slf4j
public class AdminProjetController {

    private final ModerationService moderationService;
    private final AdminService adminService;

    public record MotifRefusDTO(@NotBlank String motif) {}

    public record ProjetResumeModerationDTO(UUID id, String titre, String domaine, String porteurNom) {
        public static ProjetResumeModerationDTO from(Projet p) {
            return new ProjetResumeModerationDTO(
                    p.getId(),
                    p.getTitre(),
                    p.getDomaine().name(),
                    p.getPorteur().getNom()
            );
        }
    }

    private Projet getProjetEnAttente(UUID id) {
        return adminService.findProjetEnAttente(id);
    }

    private UUID getCurrentAdminId() {
        // À adapter selon votre mécanisme d'authentification
        return UUID.fromString("11111111-1111-1111-1111-111111111111");
    }

    // ======================== ANALYSE ========================
    @GetMapping("/{id}/analyser")
    public ResponseEntity<RapportValidationDTO> analyserProjet(@PathVariable UUID id) {
        Projet projet = getProjetEnAttente(id);

        // Filtre 1 : modération de contenu
        ModerationService.ResultatModeration moderation = moderationService.analyser(
                projet.getTitre() + " " + projet.getDescription()
        );
        ModerationResultatDTO moderationDTO = ModerationResultatDTO.from(moderation);

        // Filtre 2 : règles métier
        RegleDomaine regle = RegleDomaine.fromDomaine(projet.getDomaine());
        ReglesMetierResultatDTO reglesDTO = verifierReglesMetier(projet, regle);

        boolean estValide = !moderation.estInterdit() && reglesDTO.violations().isEmpty();

        RapportValidationDTO rapport = new RapportValidationDTO(
                projet.getId().getMostSignificantBits(),
                projet.getTitre(),
                projet.getDomaine(),
                estValide,
                moderationDTO,
                reglesDTO,
                estValide ? "Projet conforme" : "Projet non conforme"
        );

        return ResponseEntity.ok(rapport);
    }

    private ReglesMetierResultatDTO verifierReglesMetier(Projet projet, RegleDomaine regle) {
        List<ViolationDTO> violations = new ArrayList<>();

        if (projet.getDureeJours() > regle.getDureeMaxJours()) {
            violations.add(ViolationDTO.duree(
                    projet.getDureeJours(),
                    regle.getDureeMaxJours(),
                    projet.getDomaine()
            ));
        }
        if (projet.getObjectifFinancier().compareTo(regle.getObjectifMax()) > 0) {
            violations.add(ViolationDTO.objectif(
                    projet.getObjectifFinancier(),
                    regle.getObjectifMax(),
                    projet.getDomaine()
            ));
        }

        return new ReglesMetierResultatDTO(
                violations.isEmpty(),
                violations,
                regle.getDureeMaxJours(),
                regle.getObjectifMax(),
                projet.getDureeJours(),
                projet.getObjectifFinancier()
        );
    }

    // ======================== APPROBATION ========================
    @PutMapping("/{id}/approuver")
    public ResponseEntity<Map<String, Object>> approuver(@PathVariable UUID id) {
        Projet projet = getProjetEnAttente(id);
        RapportValidationDTO rapport = analyserProjet(id).getBody();

        if (rapport == null || !rapport.estValide()) {
            return ResponseEntity.unprocessableEntity().body(Map.of(
                    "statut", "REFUSÉ",
                    "raison", "Le projet ne satisfait pas les critères de validation"
            ));
        }

        adminService.approuverProjet(projet.getId(), getCurrentAdminId());
        return ResponseEntity.ok(Map.of(
                "statut", "APPROUVÉ",
                "projetId", id
        ));
    }

    // ======================== REFUS MANUEL ========================
    @PutMapping("/{id}/refuser")
    public ResponseEntity<Map<String, Object>> refuser(
            @PathVariable UUID id,
            @RequestBody @Valid MotifRefusDTO motif) {
        Projet projet = getProjetEnAttente(id);
        adminService.refuserProjet(projet.getId(), getCurrentAdminId(), motif.motif());
        return ResponseEntity.ok(Map.of(
                "statut", "REFUSÉ",
                "projetId", id,
                "motif", motif.motif()
        ));
    }

    // ======================== LISTE DES PROJETS EN ATTENTE ========================
    @GetMapping("/en-attente")
    public ResponseEntity<List<ProjetResumeModerationDTO>> listerEnAttente() {
        var projets = adminService.getProjetsEnAttente(Pageable.unpaged()).getContent();
        var dtos = projets.stream().map(ProjetResumeModerationDTO::from).toList();
        return ResponseEntity.ok(dtos);
    }
}