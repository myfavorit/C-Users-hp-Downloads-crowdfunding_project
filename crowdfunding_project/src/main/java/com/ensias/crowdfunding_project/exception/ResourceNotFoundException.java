package com.ensias.crowdfunding_project.exception;

/**
 * Exception levée lorsqu'une ressource (utilisateur, projet, etc.) n'est pas trouvée.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}