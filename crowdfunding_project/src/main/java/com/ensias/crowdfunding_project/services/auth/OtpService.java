package com.ensias.crowdfunding_project.services.auth;

import com.ensias.crowdfunding_project.entities.Utilisateur;
import com.ensias.crowdfunding_project.exception.BusinessException;
import com.ensias.crowdfunding_project.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final UtilisateurRepository utilisateurRepository;

    // ── Configuration depuis application.yml ─────────────────────────────
    @Value("${otp.length:6}")
    private int otpLength;

    @Value("${otp.expiry-minutes:15}")
    private int expiryMinutes;

    @Value("${otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${otp.max-generations:5}")
    private int maxGenerations;               // ← limite de renvois d'OTP

    private final SecureRandom secureRandom = new SecureRandom();

    // ============================================================
    // 1. GÉNÉRATION D'UN NOUVEL OTP (avec limite de renvois)
    // ============================================================

    @Transactional
    public String generateOtp(String email) {
        Optional<Utilisateur> opt = utilisateurRepository.findByEmail(email);

        // Email inconnu → retourne null (l'appelant n'enverra pas d'email)
        if (opt.isEmpty()) {
            log.debug("Tentative de génération OTP pour email inconnu");
            return null;
        }

        Utilisateur user = opt.get();

        // Vérifier la limite de générations (max 5 demandes avant activation)
        if (user.getOtpGenerationCount() >= maxGenerations) {
            throw new BusinessException("Vous avez dépassé le nombre maximum de demandes de code. Veuillez réessayer plus tard.");
        }

        // Incrémenter le compteur de générations
        utilisateurRepository.incrementOtpGenerationCount(user.getId());

        // Générer le code et le stocker
        String otp = generateRandomCode();
        user.setOtpCode(otp);
        user.setOtpExpiration(LocalDateTime.now().plusMinutes(expiryMinutes));
        user.setOtpAttempts(0);                     // réinitialise les tentatives de saisie
        utilisateurRepository.save(user);

        log.debug("OTP généré pour utilisateur ID: {} (génération {}/{})",
                user.getId(), user.getOtpGenerationCount() + 1, maxGenerations);
        return otp;
    }

    // ============================================================
    // 2. VÉRIFICATION DE L'OTP (avec protection brute force)
    // ============================================================

    @Transactional
    public boolean verifyOtp(String email, String otp) {
        Optional<Utilisateur> opt = utilisateurRepository.findByEmail(email);

        if (opt.isEmpty()) {
            log.debug("Vérification OTP pour email inconnu");
            return false;
        }

        Utilisateur user = opt.get();

        // OTP absent ou expiré
        if (user.getOtpCode() == null
                || user.getOtpExpiration() == null
                || user.getOtpExpiration().isBefore(LocalDateTime.now())) {
            log.debug("OTP absent ou expiré pour utilisateur ID: {}", user.getId());
            if (user.getOtpCode() != null) {
                utilisateurRepository.clearOtp(user.getId());
            }
            return false;
        }

        // Trop de tentatives → invalider l'OTP
        if (user.getOtpAttempts() >= maxAttempts) {
            log.warn("Trop de tentatives OTP pour utilisateur ID: {} — OTP invalidé", user.getId());
            utilisateurRepository.clearOtp(user.getId());
            return false;
        }

        // Incrémenter les tentatives AVANT la vérification
        utilisateurRepository.incrementOtpAttempts(user.getId());

        // Vérification du code
        if (user.getOtpCode().equals(otp)) {
            utilisateurRepository.clearOtp(user.getId());   // usage unique
            log.debug("OTP valide pour utilisateur ID: {}", user.getId());
            return true;
        }

        log.debug("OTP incorrect pour utilisateur ID: {}", user.getId());
        return false;
    }

    // ============================================================
    // 3. NETTOYAGE MANUEL
    // ============================================================

    @Transactional
    public void clearOtp(String email) {
        utilisateurRepository.findByEmail(email).ifPresent(user -> {
            utilisateurRepository.clearOtp(user.getId());
            log.debug("OTP effacé pour utilisateur ID: {}", user.getId());
        });
    }

    // ============================================================
    // 4. UTILITAIRE PRIVÉ
    // ============================================================

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }
    public boolean emailExists(String email) {
        return utilisateurRepository.existsByEmail(email);
    }
}