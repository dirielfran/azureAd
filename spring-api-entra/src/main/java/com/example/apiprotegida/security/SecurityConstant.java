package com.example.apiprotegida.security;

/**
 * Constantes de seguridad para el sistema de autenticación
 * Incluye constantes para JWT y Azure AD
 */
public class SecurityConstant {
    
    // =============================================================================
    // CONSTANTES PARA JWT
    // =============================================================================
    
    /** Prefijo del token JWT en el header Authorization */
    public static final String TOKEN_PREFIX = "Bearer ";
    
    /** Nombre de la aplicación para el issuer del JWT */
    public static final String API_TYC = "API_TYC";
    
    /** Clave para el claim de autoridades en el JWT */
    public static final String AUTHORITIES = "authorities";
    
    /** Tiempo de expiración del token JWT en milisegundos (24 horas) */
    public static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24 horas
    
    /** Mensaje de error cuando no se puede verificar el token */
    public static final String TOKEN_NO_SE_PUEDE_VERIFICAR = "El token no se puede verificar";
    
    // =============================================================================
    // CONSTANTES PARA AZURE AD
    // =============================================================================
    
    /** Prefijo para grupos de Azure AD en las autoridades */
    public static final String AZURE_GROUP_PREFIX = "GROUP_";
    
    /** Prefijo para roles de Azure AD */
    public static final String AZURE_ROLE_PREFIX = "ROLE_";
    
    // =============================================================================
    // CONSTANTES PARA HEADERS HTTP
    // =============================================================================
    
    /** Header de autorización */
    public static final String AUTHORIZATION_HEADER = "Authorization";
    
    /** Header para información del usuario */
    public static final String USER_INFO_HEADER = "X-User-Info";
    
    // =============================================================================
    // CONSTANTES PARA ENDPOINTS
    // =============================================================================
    
    /** Endpoint de login JWT */
    public static final String LOGIN_ENDPOINT = "/auth/login";
    
    /** Endpoint de información de usuario */
    public static final String USER_INFO_ENDPOINT = "/auth/info";
    
    /** Endpoint de refresh token */
    public static final String REFRESH_TOKEN_ENDPOINT = "/auth/refresh";
    
    // =============================================================================
    // CONSTANTES PARA PERFILES Y PERMISOS
    // =============================================================================
    
    /** Perfil por defecto para usuarios sin grupo específico */
    public static final String DEFAULT_PROFILE = "Usuario Básico";
    
    /** Prefijo para permisos en las autoridades */
    public static final String PERMISSION_PREFIX = "";
    
    /** Prefijo para roles en las autoridades */
    public static final String ROLE_PREFIX = "ROLE_";
    
    // =============================================================================
    // CONSTANTES PARA CONFIGURACIÓN
    // =============================================================================
    
    /** Tamaño mínimo de la clave secreta JWT */
    public static final int MIN_SECRET_KEY_LENGTH = 32;
    
    /** Algoritmo de firma JWT */
    public static final String JWT_ALGORITHM = "HMAC512";
    
    // =============================================================================
    // MÉTODOS UTILITARIOS
    // =============================================================================
    
    /**
     * Verifica si un token tiene el prefijo correcto
     * @param token El token a verificar
     * @return true si tiene el prefijo correcto
     */
    public static boolean hasTokenPrefix(String token) {
        return token != null && token.startsWith(TOKEN_PREFIX);
    }
    
    /**
     * Extrae el token sin el prefijo
     * @param token El token con prefijo
     * @return El token sin prefijo
     */
    public static String extractToken(String token) {
        if (hasTokenPrefix(token)) {
            return token.substring(TOKEN_PREFIX.length());
        }
        return token;
    }
    
    /**
     * Crea un token con prefijo
     * @param token El token sin prefijo
     * @return El token con prefijo
     */
    public static String addTokenPrefix(String token) {
        if (token != null && !hasTokenPrefix(token)) {
            return TOKEN_PREFIX + token;
        }
        return token;
    }
}
