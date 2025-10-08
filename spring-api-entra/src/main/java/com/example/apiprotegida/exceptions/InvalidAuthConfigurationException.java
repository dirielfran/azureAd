package com.example.apiprotegida.exceptions;

/**
 * Excepción lanzada cuando se intenta aplicar una configuración de autenticación inválida
 * que dejaría al sistema sin ningún método de autenticación activo.
 */
public class InvalidAuthConfigurationException extends RuntimeException {
    
    private final boolean currentAzureEnabled;
    private final boolean currentJwtLocalEnabled;
    
    public InvalidAuthConfigurationException(String message, boolean currentAzureEnabled, boolean currentJwtLocalEnabled) {
        super(message);
        this.currentAzureEnabled = currentAzureEnabled;
        this.currentJwtLocalEnabled = currentJwtLocalEnabled;
    }
    
    public boolean isCurrentAzureEnabled() {
        return currentAzureEnabled;
    }
    
    public boolean isCurrentJwtLocalEnabled() {
        return currentJwtLocalEnabled;
    }
}

