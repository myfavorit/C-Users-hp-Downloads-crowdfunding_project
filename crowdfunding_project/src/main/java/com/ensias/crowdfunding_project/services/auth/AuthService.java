package com.ensias.crowdfunding_project.services.auth;

import com.ensias.crowdfunding_project.dto.auth.*;
import com.ensias.crowdfunding_project.entities.Utilisateur;
import com.ensias.crowdfunding_project.enums.RoleUtilisateur;
import com.ensias.crowdfunding_project.enums.StatutCompte;
import com.ensias.crowdfunding_project.exception.BusinessException;
import com.ensias.crowdfunding_project.exception.ResourceNotFoundException;
import com.ensias.crowdfunding_project.repositories.UtilisateurRepository;
import com.ensias.crowdfunding_project.security.Service.CustomUserDetailsService;
import com.ensias.crowdfunding_project.security.Service.JwtService;
import com.ensias.crowdfunding_project.services.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    // ✅ TOUTES les dépendances déclarées ici en haut
    private final UtilisateurRepository    utilisateurRepository;
    private final PasswordEncoder          passwordEncoder;
    private final JwtService               jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationManager    authenticationManager;
    private final OtpService               otpService;
    private final EmailService             emailService;
    private final LoginAttemptService      loginAttemptService; // ✅ REMONTE ICI

    // ================================================================
    // 1. INSCRIPTION
    // ================================================================
    @Transactional
    public void register(RegisterRequest request) {
        log.info("Tentative d inscription avec l email : {}", request.getEmail());

        validatePasswordComplexity(request.getMotDePasse());

        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Un compte avec cet email existe deja.");
        }

        RoleUtilisateur role;
        try {
            role = RoleUtilisateur.valueOf(request.getRole());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Role invalide. Valeurs acceptees : INVESTOR, PROJECT_CREATOR");
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .email(request.getEmail())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .motDePasseHash(passwordEncoder.encode(request.getMotDePasse()))
                .role(role)
                .statut(StatutCompte.INACTIF)
                .build();

        utilisateur = utilisateurRepository.save(utilisateur);
        log.info("Utilisateur cree (INACTIF) : {}", utilisateur.getId());

        String otp = otpService.generateOtp(request.getEmail());
        if (otp == null) {
            throw new BusinessException("Impossible de generer l OTP. Veuillez reessayer.");
        }
        emailService.sendOtpEmail(request.getEmail(), request.getPrenom(), otp);
        log.debug("OTP envoye a {}", request.getEmail());
    }

    // ================================================================
    // 2. RENVOI D'OTP
    // ================================================================
    @Transactional
    public void resendOtp(String email) {
        log.debug("Demande de renvoi d OTP pour {}", email);

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));

        if (utilisateur.getStatut() != StatutCompte.INACTIF) {
            throw new BusinessException("Ce compte est deja actif. Connectez-vous.");
        }

        String otp = otpService.generateOtp(email);
        if (otp == null) {
            throw new BusinessException("Erreur technique. Veuillez reessayer.");
        }
        emailService.sendOtpEmail(email, utilisateur.getPrenom(), otp);
        log.info("Nouvel OTP envoye a {}", email);
    }

    // ================================================================
    // 3. VERIFICATION OTP
    // ================================================================
    @Transactional
    public AuthResponse verifyOtp(OtpVerificationRequest request) {
        log.info("Verification OTP pour l email : {}", request.getEmail());

        if (!otpService.verifyOtp(request.getEmail(), request.getOtp())) {
            throw new BusinessException("Code OTP invalide ou expire.");
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));

        utilisateur.setStatut(StatutCompte.ACTIF);
        utilisateurRepository.save(utilisateur);
        log.info("Compte active pour {}", request.getEmail());

        UserDetails userDetails = userDetailsService.loadUserByUsername(utilisateur.getEmail());
        String token    = jwtService.generateToken(userDetails);
        long expiresIn  = jwtService.getTimeRemaining(token);

        emailService.sendConfirmationEmail(utilisateur.getEmail(), utilisateur.getPrenom());

        return buildAuthResponse(utilisateur, token, expiresIn);
    }

    // ================================================================
    // 4. CONNEXION
    // ================================================================
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Tentative de connexion pour l email : {}", request.getEmail());

        loginAttemptService.checkNotLocked(request.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getMotDePasse()
                    )
            );
        } catch (BadCredentialsException e) {
            log.warn("Echec de connexion pour {} : mauvais identifiants", request.getEmail());
            loginAttemptService.registerFailedAttempt(request.getEmail());
            throw new BusinessException("Email ou mot de passe incorrect.");
        } catch (DisabledException e) {
            log.warn("Compte desactive pour {}", request.getEmail());
            throw new BusinessException("Compte desactive. Contactez l administrateur.");
        } catch (LockedException e) {
            log.warn("Compte verrouille pour {}", request.getEmail());
            throw new BusinessException("Compte verrouille. Veuillez reessayer plus tard.");
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));

        if (utilisateur.getStatut() != StatutCompte.ACTIF) {
            log.warn("Tentative de connexion sur un compte inactif : {}", request.getEmail());
            throw new BusinessException("Compte inactif. Veuillez verifier votre email.");
        }

        loginAttemptService.registerSuccess(request.getEmail());

        UserDetails userDetails = userDetailsService.loadUserByUsername(utilisateur.getEmail());
        String token   = jwtService.generateToken(userDetails);
        long expiresIn = jwtService.getTimeRemaining(token);

        return buildAuthResponse(utilisateur, token, expiresIn);
    }

    // ================================================================
    // 5. RAFRAICHISSEMENT DE TOKEN
    // ================================================================
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String oldToken) {
        log.debug("Tentative de rafraichissement de token");

        String email = jwtService.extractUsername(oldToken);
        if (email == null) {
            throw new BusinessException("Token invalide.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        if (!jwtService.isTokenValid(oldToken, userDetails)) {
            throw new BusinessException("Token expire ou invalide.");
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));

        String newToken = jwtService.generateToken(userDetails);
        long expiresIn  = jwtService.getTimeRemaining(newToken);

        return buildAuthResponse(utilisateur, newToken, expiresIn);
    }

    // ================================================================
    // 6. MOT DE PASSE OUBLIE
    // ================================================================
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        log.debug("Demande de reinitialisation pour l email : {}", request.getEmail());

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (utilisateur == null) {
            log.debug("Email non trouve : {}", request.getEmail());
            return; // message generique — evite l enumeration
        }

        String resetToken = UUID.randomUUID().toString();
        utilisateur.setResetToken(resetToken);
        utilisateur.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        utilisateurRepository.save(utilisateur);

        emailService.sendResetPasswordEmail(
                utilisateur.getEmail(), utilisateur.getPrenom(), resetToken);
        log.info("Email de reinitialisation envoye pour {}", request.getEmail());
    }

    // ================================================================
    // 7. REINITIALISATION DU MOT DE PASSE
    // ================================================================
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.debug("Tentative de reinitialisation avec token");

        validatePasswordComplexity(request.getNewPassword());

        Utilisateur utilisateur = utilisateurRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new BusinessException("Token invalide ou expire."));

        if (utilisateur.getResetTokenExpiry() == null ||
                utilisateur.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Le token a expire. Veuillez refaire une demande.");
        }

        utilisateur.setMotDePasseHash(passwordEncoder.encode(request.getNewPassword()));
        utilisateur.setResetToken(null);
        utilisateur.setResetTokenExpiry(null);
        utilisateurRepository.save(utilisateur);

        log.info("Mot de passe reinitialise pour : {}", utilisateur.getEmail());
    }

    // ================================================================
    // 8. DECONNEXION (stateless)
    // ================================================================
    public void logout() {
        log.debug("Deconnexion - le client doit supprimer le token localement.");
    }

    // ================================================================
    // 9. UTILITAIRES
    // ================================================================
    public boolean emailExists(String email) {
        return utilisateurRepository.existsByEmail(email);
    }

    // ================================================================
    // METHODES PRIVEES
    // ================================================================

    private void validatePasswordComplexity(String password) {
        if (password == null || password.isBlank()) {
            throw new BusinessException("Le mot de passe ne peut pas etre vide.");
        }
        if (password.length() < 8) {
            throw new BusinessException("Le mot de passe doit contenir au moins 8 caracteres.");
        }
        if (password.length() > 72) {
            throw new BusinessException("Le mot de passe ne peut pas depasser 72 caracteres.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new BusinessException("Le mot de passe doit contenir au moins une majuscule.");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new BusinessException("Le mot de passe doit contenir au moins une minuscule.");
        }
        if (!password.matches(".*\\d.*")) {
            throw new BusinessException("Le mot de passe doit contenir au moins un chiffre.");
        }
        if (!password.matches(".*[@#$%^&+=!?].*")) {
            throw new BusinessException("Le mot de passe doit contenir au moins un caractere special (@#$%^&+=!?).");
        }
    }

    private AuthResponse buildAuthResponse(Utilisateur utilisateur,
                                           String token,
                                           long expiresIn) {
        boolean kycValide = utilisateur.getProfilKyc() != null
                && utilisateur.getProfilKyc().isKycValide();

        return AuthResponse.builder()
                .userId(utilisateur.getId())
                .token(token)
                .email(utilisateur.getEmail())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .role(utilisateur.getRole().name())
                .statut(utilisateur.getStatut().name())
                .kycValide(kycValide)
                .expiresIn(expiresIn)
                .build();
    }
}