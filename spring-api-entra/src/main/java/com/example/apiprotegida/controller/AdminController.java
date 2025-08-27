package com.example.apiprotegida.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para funciones administrativas y configuración de usuarios
 */
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = {"http://localhost:4200", "https://localhost:4200"})
public class AdminController {

    /**
     * Obtiene información detallada del usuario actual para configuración
     */
    @GetMapping("/user-details")
    public ResponseEntity<Map<String, Object>> getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok(Map.of("error", "Usuario no autenticado"));
        }

        Map<String, Object> userDetails = new HashMap<>();
        
        // Información básica
        userDetails.put("name", authentication.getName());
        userDetails.put("authorities", authentication.getAuthorities());
        userDetails.put("authenticated", authentication.isAuthenticated());
        
        // Si es un JWT, extraer más información
        if (authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            
            // Información del token usando HashMap para evitar límite de Map.of()
            Map<String, Object> tokenClaims = new HashMap<>();
            tokenClaims.put("sub", jwt.getClaimAsString("sub"));
            tokenClaims.put("name", jwt.getClaimAsString("name"));
            tokenClaims.put("email", jwt.getClaimAsString("email"));
            tokenClaims.put("preferred_username", jwt.getClaimAsString("preferred_username"));
            tokenClaims.put("upn", jwt.getClaimAsString("upn"));
            tokenClaims.put("given_name", jwt.getClaimAsString("given_name"));
            tokenClaims.put("family_name", jwt.getClaimAsString("family_name"));
            tokenClaims.put("aud", jwt.getAudience());
            tokenClaims.put("iss", jwt.getIssuer());
            tokenClaims.put("iat", jwt.getIssuedAt());
            tokenClaims.put("exp", jwt.getExpiresAt());
            
            userDetails.put("tokenClaims", tokenClaims);
            
            // Información para configuración de roles
            String email = getUserEmail(jwt);
            userDetails.put("configInfo", Map.of(
                "email", email,
                "suggestedRoleMapping", "\"" + email + "\", \"ROLE_ADMIN\"",
                "instructions", "Agrega esta línea al USER_TO_ROLE_MAPPING en AzureAdGroupsJwtConverter.java"
            ));
        }
        
        return ResponseEntity.ok(userDetails);
    }

    /**
     * Endpoint para probar diferentes niveles de acceso
     */
    @GetMapping("/test-admin")
    public ResponseEntity<Map<String, Object>> testAdminAccess() {
        return ResponseEntity.ok(Map.of(
            "message", "✅ Acceso de ADMINISTRADOR concedido",
            "level", "ADMIN",
            "timestamp", System.currentTimeMillis()
        ));
    }

    @GetMapping("/test-manager") 
    public ResponseEntity<Map<String, Object>> testManagerAccess() {
        return ResponseEntity.ok(Map.of(
            "message", "✅ Acceso de GESTOR concedido",
            "level", "MANAGER",
            "timestamp", System.currentTimeMillis()
        ));
    }

    @GetMapping("/test-user")
    public ResponseEntity<Map<String, Object>> testUserAccess() {
        return ResponseEntity.ok(Map.of(
            "message", "✅ Acceso de USUARIO concedido", 
            "level", "USER",
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Helper method para extraer email del JWT
     */
    private String getUserEmail(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email == null) {
            email = jwt.getClaimAsString("preferred_username");
        }
        if (email == null) {
            email = jwt.getClaimAsString("upn");
        }
        return email != null ? email.toLowerCase() : "unknown";
    }
}
