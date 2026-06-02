package com.ensias.crowdfunding_project.security.Service;

import com.ensias.crowdfunding_project.entities.Utilisateur;
import com.ensias.crowdfunding_project.enums.StatutCompte;
import com.ensias.crowdfunding_project.repositories.UtilisateurRepository;
import com.ensias.crowdfunding_project.security.user.CustomUserDetails; // ← ajouter import
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Tentative de chargement de l'utilisateur: {}", email);

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Tentative de connexion avec email inexistant: {}", email);
                    return new UsernameNotFoundException("Identifiants invalides");
                });

        log.debug("Utilisateur trouvé: {}, Rôle: {}, Statut: {}",
                utilisateur.getEmail(), utilisateur.getRole(), utilisateur.getStatut());

        if (utilisateur.getStatut() == StatutCompte.SUSPENDU) {
            log.warn("Compte SUSPENDU: {}", email);
            throw new UsernameNotFoundException("Identifiants invalides");
        }

        if (utilisateur.getStatut() == StatutCompte.BANNI) {
            log.warn("Compte BANNI: {}", email);
            throw new UsernameNotFoundException("Identifiants invalides");
        }

        if (utilisateur.getStatut() != StatutCompte.ACTIF) {
            log.warn("Compte statut inconnu: {} - {}", email, utilisateur.getStatut());
            throw new UsernameNotFoundException("Identifiants invalides");
        }

        // ✅ CORRECTION — retourner CustomUserDetails au lieu de User Spring standard
        return new CustomUserDetails(utilisateur); // ← SEUL CHANGEMENT
    }
}