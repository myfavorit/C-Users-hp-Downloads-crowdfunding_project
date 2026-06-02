package com.ensias.crowdfunding_project.exception;

/**
 * Exception métier générique pour les erreurs fonctionnelles
 * (ex: email déjà existant, OTP invalide, action non autorisée).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}