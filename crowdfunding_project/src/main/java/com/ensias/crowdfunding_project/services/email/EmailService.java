package com.ensias.crowdfunding_project.services.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.email.disabled:false}")
    private boolean emailDisabled;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender,
                        SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    // ── 1. OTP ────────────────────────────────────────────────
    @Async
    public void sendOtpEmail(String to, String prenom, String otp) {
        if (emailDisabled) {
            log.warn("╔══════════════════════════════════════╗");
            log.warn("║  [DEV] OTP pour {}", to);
            log.warn("║  Code : {}", otp);
            log.warn("╚══════════════════════════════════════╝");
            return;
        }
        try {
            Context ctx = new Context();
            ctx.setVariables(Map.of(
                    "prenom", prenom,
                    "otp", otp,
                    "expiryMinutes", 15
            ));
            String html = templateEngine.process("otp", ctx);
            sendHtmlEmail(to, "Votre code de vérification", html);
            log.info("Email OTP envoyé à {}", to);
        } catch (Exception e) {
            log.error("Erreur envoi OTP à {} : {}", to, e.getMessage());
        }
    }

    // ── 2. Réinitialisation mot de passe ──────────────────────
    @Async
    public void sendResetPasswordEmail(String to, String prenom, String token) {
        if (emailDisabled) {
            log.warn("╔══════════════════════════════════════════════════╗");
            log.warn("║  [DEV] Reset token pour {}", to);
            log.warn("║  Token : {}", token);
            log.warn("║  URL   : {}/auth/reset-password?token={}", frontendUrl, token);
            log.warn("╚══════════════════════════════════════════════════╝");
            return;
        }
        try {
            String resetUrl = frontendUrl + "/auth/reset-password?token=" + token;
            Context ctx = new Context();
            ctx.setVariables(Map.of(
                    "prenom", prenom,
                    "resetUrl", resetUrl,
                    "expiryHours", 1
            ));
            String html = templateEngine.process("reset-password", ctx);
            sendHtmlEmail(to, "Réinitialisation de votre mot de passe", html);
            log.info("Email reset password envoyé à {}", to);
        } catch (Exception e) {
            log.error("Erreur envoi reset password à {} : {}", to, e.getMessage());
        }
    }

    // ── 3. Confirmation d'inscription ─────────────────────────
    @Async
    public void sendConfirmationEmail(String to, String prenom) {
        if (emailDisabled) {
            log.warn("[DEV] Email confirmation simulé pour {}", to);
            return;
        }
        try {
            Context ctx = new Context();
            ctx.setVariables(Map.of(
                    "prenom", prenom,
                    "loginUrl", frontendUrl + "/auth/login"
            ));
            String html = templateEngine.process("confirmation", ctx);
            sendHtmlEmail(to, "Bienvenue sur CrowdFund ENSIAS !", html);
            log.info("Email confirmation envoyé à {}", to);
        } catch (Exception e) {
            log.error("Erreur envoi confirmation à {} : {}", to, e.getMessage());
        }
    }

    // ── Méthode privée commune ────────────────────────────────
    private void sendHtmlEmail(String to, String subject, String htmlContent)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message, true, "UTF-8"
        );
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = HTML
        mailSender.send(message);
    }
}