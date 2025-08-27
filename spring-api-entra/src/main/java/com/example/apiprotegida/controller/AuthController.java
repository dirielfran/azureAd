package com.example.apiprotegida.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para endpoints relacionados con autenticación
 * 
 * Proporciona información sobre el usuario autenticado y
 * endpoints para verificar la autenticación.
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:4200", "https://localhost:4200"})
public class AuthController {

    /**
     * Endpoint público que proporciona información sobre la API
     * @return Información básica de la API
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getApiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("api", "API Protegida con Microsoft Entra ID");
        info.put("version", "1.0.0");
        info.put("descripcion", "API REST segura integrada con Azure AD");
        info.put("endpoints", Map.of(
            "usuarios", "/users",
            "datos", "/data",
            "perfil", "/profile",
            "autenticacion", "/auth"
        ));
        info.put("autenticacion", "Microsoft Entra ID (Azure AD)");
        info.put("scopes_requeridos", "access_as_user");
        
        return ResponseEntity.ok(info);
    }

    /**
     * Obtiene información del usuario autenticado actual
     * @return Información del usuario desde el token JWT
     */
    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Usuario no autenticado"));
        }

        Map<String, Object> userInfo = new HashMap<>();
        
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            // Extraer información del token JWT
            userInfo.put("sub", jwt.getClaimAsString("sub"));
            userInfo.put("name", jwt.getClaimAsString("name"));
            userInfo.put("preferred_username", jwt.getClaimAsString("preferred_username"));
            userInfo.put("email", jwt.getClaimAsString("email"));
            userInfo.put("given_name", jwt.getClaimAsString("given_name"));
            userInfo.put("family_name", jwt.getClaimAsString("family_name"));
            userInfo.put("oid", jwt.getClaimAsString("oid")); // Object ID en Azure AD
            userInfo.put("tid", jwt.getClaimAsString("tid")); // Tenant ID
            userInfo.put("scopes", jwt.getClaimAsStringList("scp"));
            userInfo.put("roles", jwt.getClaimAsStringList("roles"));
            userInfo.put("aud", jwt.getAudience());
            userInfo.put("iss", jwt.getIssuer());
            userInfo.put("exp", jwt.getExpiresAt());
            userInfo.put("iat", jwt.getIssuedAt());
        }
        
        userInfo.put("authorities", authentication.getAuthorities());
        userInfo.put("authenticated", authentication.isAuthenticated());
        
        return ResponseEntity.ok(userInfo);
    }

    /**
     * Verifica si el usuario tiene un scope específico
     * @param scope El scope a verificar
     * @return Resultado de la verificación
     */
    @GetMapping("/verify-scope")
    public ResponseEntity<Map<String, Object>> verifyScope(@RequestParam String scope) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> result = new HashMap<>();
        result.put("scope", scope);
        result.put("hasScope", false);
        
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            var scopes = jwt.getClaimAsStringList("scp");
            boolean hasScope = scopes != null && scopes.contains(scope);
            result.put("hasScope", hasScope);
            result.put("userScopes", scopes);
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint para verificar que la API está funcionando y el usuario está autenticado
     * @return Mensaje de confirmación
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "¡Autenticación exitosa!");
        response.put("timestamp", System.currentTimeMillis());
        response.put("authenticated", authentication != null && authentication.isAuthenticated());
        
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            response.put("user", jwt.getClaimAsString("name"));
            response.put("email", jwt.getClaimAsString("email"));
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene los claims completos del JWT token (para debugging)
     * @return Todos los claims del token
     */
    @GetMapping("/token-claims")
    public ResponseEntity<Map<String, Object>> getTokenClaims() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return ResponseEntity.status(401).body(Map.of("error", "No JWT token found"));
        }
        
        Map<String, Object> claims = new HashMap<>(jwt.getClaims());
        
        // Agregar información adicional
        claims.put("_debug_info", Map.of(
            "token_type", "Bearer",
            "algorithm", jwt.getHeaders().get("alg"),
            "key_id", jwt.getHeaders().get("kid"),
            "expires_in_seconds", jwt.getExpiresAt().getEpochSecond() - System.currentTimeMillis() / 1000
        ));
        
        return ResponseEntity.ok(claims);
    }
}
