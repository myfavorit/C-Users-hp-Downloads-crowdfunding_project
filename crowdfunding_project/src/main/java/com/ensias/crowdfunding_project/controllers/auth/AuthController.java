package com.ensias.crowdfunding_project.controllers.auth;

import com.ensias.crowdfunding_project.dto.auth.*;
import com.ensias.crowdfunding_project.exception.BusinessException;
import com.ensias.crowdfunding_project.services.auth.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthController {

    private final AuthService authService;

    // ============================================================
    // 1. INSCRIPTION (envoi OTP)
    // ============================================================
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void register(@Valid @RequestBody RegisterRequest request) {
        log.info("Requete d'inscription pour {}", request.getEmail());
        authService.register(request);
    }

    // ============================================================
    // 2. RENVOI D'OTP
    // ============================================================
    @PostMapping("/resend-otp")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void resendOtp(@RequestParam @NotBlank @Email String email) {
        log.info("Demande de renvoi d'OTP pour {}", email);
        authService.resendOtp(email);
    }

    // ============================================================
    // 3. VERIFICATION OTP + ACTIVATION COMPTE
    // ============================================================
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(
            @Valid @RequestBody OtpVerificationRequest request) {
        log.info("Verification OTP pour {}", request.getEmail());
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    // ============================================================
    // 4. CONNEXION (email + mot de passe)
    // ============================================================
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("Tentative de connexion pour {}", request.getEmail());
        return ResponseEntity.ok(authService.login(request));
    }

    // ============================================================
    // 5. RAFRAICHISSEMENT DU TOKEN
    // ============================================================
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("Token invalide ou manquant.");
        }
        return ResponseEntity.ok(authService.refreshToken(authHeader.substring(7)));
    }

    // ============================================================
    // 6. MOT DE PASSE OUBLIE
    // ============================================================
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Demande de reinitialisation pour {}", request.getEmail());
        authService.forgotPassword(request);
    }

    // ============================================================
    // 7. REINITIALISATION DU MOT DE PASSE
    // ============================================================
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.OK)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Tentative de reinitialisation avec token");
        authService.resetPassword(request);
    }

    // ============================================================
    // 8. DECONNEXION (stateless)
    // ============================================================
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout() {
        log.info("Deconnexion - suppression du token cote client");
        authService.logout();
    }

    // ============================================================
    // 9. VERIFICATION EMAIL (utilitaire frontend)
    // ============================================================
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(
            @RequestParam @NotBlank @Email String email) {
        return ResponseEntity.ok(authService.emailExists(email));
    }
}