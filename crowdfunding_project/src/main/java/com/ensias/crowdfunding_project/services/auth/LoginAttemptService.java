package com.ensias.crowdfunding_project.services.auth;

import com.ensias.crowdfunding_project.entities.Utilisateur;
import com.ensias.crowdfunding_project.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {

    private final UtilisateurRepository utilisateurRepository;

    @Value("${security.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.login.lock-duration-minutes:30}")
    private int lockDurationMinutes;

    // ── Appelé depuis AuthService après BadCredentialsException ──

    @Transactional
    public void registerFailedAttempt(String email) {
        Optional<Utilisateur> opt = utilisateurRepository.findByEmail(email);
        if (opt.isEmpty()) return;

        Utilisateur user = opt.get();
        utilisateurRepository.incrementLoginAttempts(email);
        int attempts = user.getLoginAttempts() + 1;

        if (attempts >= maxAttempts) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(lockDurationMinutes);
            utilisateurRepository.lockUntil(email, lockUntil);
            log.warn("Compte verrouillé jusqu'à {} pour l'email {}", lockUntil, email);
        }

        log.debug("Tentatives échouées pour {} : {}/{}", email, attempts, maxAttempts);
    }

    // ── Appelé depuis AuthService après connexion réussie ────────

    @Transactional
    public void registerSuccess(String email) {
        utilisateurRepository.resetLoginAttempts(email);
        log.debug("Compteur d'échecs réinitialisé pour {}", email);
    }

    // ── Vérification avant authentification ──────────────────────

    @Transactional(readOnly = true)
    public void checkNotLocked(String email) {
        Optional<Utilisateur> opt = utilisateurRepository.findByEmail(email);
        if (opt.isEmpty()) return;

        Utilisateur user = opt.get();
        if (user.getLockedUntil() != null
                && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            long minutesLeft = java.time.Duration
                    .between(LocalDateTime.now(), user.getLockedUntil())
                    .toMinutes() + 1;
            throw new com.ensias.crowdfunding_project.exception.BusinessException(
                    "Compte temporairement verrouillé. Réessayez dans "
                            + minutesLeft + " minute(s).");
        }
    }
}