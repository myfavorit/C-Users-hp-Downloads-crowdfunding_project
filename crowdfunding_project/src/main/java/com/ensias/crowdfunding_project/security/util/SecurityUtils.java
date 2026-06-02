package com.ensias.crowdfunding_project.security.util;

import com.ensias.crowdfunding_project.security.user.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class SecurityUtils {

    /**
     * Récupère l'UUID de l'utilisateur connecté depuis le SecurityContext.
     * Utilise CustomUserDetails qui expose getId() — miroir de CustomUserDetailsService
     * qui retourne désormais new CustomUserDetails(utilisateur).
     */
    public static UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }

        // ← Cast vers CustomUserDetails qui expose getId()
        if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId(); // UUID réel de l'utilisateur en base
        }

        throw new RuntimeException(
                "Principal inattendu : " + auth.getPrincipal().getClass().getName()
                        + ". Vérifiez que CustomUserDetailsService retourne bien new CustomUserDetails(utilisateur)."
        );
    }

    /**
     * Récupère l'email de l'utilisateur connecté.
     * Utile pour les logs ou les vérifications sans accès à la base.
     */
    public static String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }

        return auth.getName(); // email — défini par getUsername() dans CustomUserDetails
    }
}