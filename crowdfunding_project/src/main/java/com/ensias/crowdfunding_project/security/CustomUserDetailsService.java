package com.ensias.crowdfunding_project.security;

import com.ensias.crowdfunding_project.entities.Utilisateur;
import com.ensias.crowdfunding_project.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        log.debug("Tentative de chargement de l'utilisateur: {}", email);

        // 1. Recherche l'utilisateur - Message générique pour éviter la révélation d'email
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Tentative de connexion avec email inexistant: {}", email);
                    // ✅ Correction 1: Message générique (ne révèle pas si l'email existe)
                    return new UsernameNotFoundException("Identifiants invalides");
                });

        log.debug("Utilisateur trouvé: {}, Rôle: {}, Statut: {}",
                utilisateur.getEmail(), utilisateur.getRole(), utilisateur.getStatut());

        // 2. Vérification du statut du compte avec messages différenciés en log mais génériques en réponse
        if (utilisateur.getStatut() == Utilisateur.StatutCompte.SUSPENDU) {
            log.warn("Tentative de connexion sur compte SUSPENDU: {}", email);
            throw new UsernameNotFoundException("Identifiants invalides");
        }

        if (utilisateur.getStatut() == Utilisateur.StatutCompte.BANNI) {
            log.warn("Tentative de connexion sur compte BANNI: {}", email);
            throw new UsernameNotFoundException("Identifiants invalides");
        }

        if (utilisateur.getStatut() != Utilisateur.StatutCompte.ACTIF) {
            log.warn("Tentative de connexion sur compte au statut inconnu: {} - {}", email, utilisateur.getStatut());
            throw new UsernameNotFoundException("Identifiants invalides");
        }

        // 3. Gestion du mot de passe pour les utilisateurs OAuth
        // ✅ Correction 2: Si motDePasseHash = null, générer un mot de passe factice
        String password = utilisateur.getMotDePasseHash();
        if (password == null) {
            // Utilisateur OAuth - mot de passe généré (ne sera jamais utilisé car l'auth se fait via OAuth)
            password = "OAUTH_USER_NO_PASSWORD";
            log.debug("Utilisateur OAuth détecté: {}", email);
        }

        // 4. Construction de l'objet UserDetails
        return new User(
                utilisateur.getEmail(),
                password,
                getAuthorities(utilisateur)
        );
    }

    private List<SimpleGrantedAuthority> getAuthorities(Utilisateur utilisateur) {
        String role = "ROLE_" + utilisateur.getRole().name();
        log.debug("Attribution du rôle: {}", role);
        return List.of(new SimpleGrantedAuthority(role));
    }
}