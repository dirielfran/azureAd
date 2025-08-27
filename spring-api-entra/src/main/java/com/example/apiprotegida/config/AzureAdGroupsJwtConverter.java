package com.example.apiprotegida.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Convertidor personalizado para extraer grupos de Azure AD desde el JWT
 * y convertirlos en authorities de Spring Security
 */
@Component
public class AzureAdGroupsJwtConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    // Mapeo de usuarios específicos a roles (usando email o UPN)
    private static final Map<String, String> USER_TO_ROLE_MAPPING = Map.of(
        // Reemplaza estos emails con los usuarios reales de tu organización
        "admin@tudominio.com", "ROLE_ADMIN",
        "gestor@tudominio.com", "ROLE_MANAGER",
        "usuario@tudominio.com", "ROLE_USER"
        // Agrega más usuarios según necesites
    );

    // También podemos usar dominios para asignar roles por defecto
    private static final Map<String, String> DOMAIN_TO_ROLE_MAPPING = Map.of(
        "@tudominio.com", "ROLE_USER"  // Todos los usuarios de tu dominio tendrán rol USER por defecto
    );

    @Override
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        // Extraer información del usuario del token JWT
        String userEmail = getUserEmail(jwt);
        String userName = getUserName(jwt);
        
        System.out.println("👤 Usuario autenticado: " + userName + " (" + userEmail + ")");
        
        // Asignar rol basado en el usuario específico
        String role = getUserRole(userEmail, userName);
        if (role != null) {
            authorities.add(new SimpleGrantedAuthority(role));
            System.out.println("✅ Rol asignado: " + userEmail + " -> " + role);
        }
        
        // Agregar scope por defecto para usuarios autenticados
        authorities.add(new SimpleGrantedAuthority("SCOPE_access_as_user"));
        
        System.out.println("🔐 Authorities finales: " + authorities);
        return authorities;
    }

    /**
     * Extrae el email del usuario del token JWT
     */
    private String getUserEmail(Jwt jwt) {
        // El email puede venir en diferentes claims
        String email = jwt.getClaimAsString("email");
        if (email == null) {
            email = jwt.getClaimAsString("preferred_username");
        }
        if (email == null) {
            email = jwt.getClaimAsString("upn"); // User Principal Name
        }
        return email != null ? email.toLowerCase() : "";
    }

    /**
     * Extrae el nombre del usuario del token JWT
     */
    private String getUserName(Jwt jwt) {
        String name = jwt.getClaimAsString("name");
        if (name == null) {
            name = jwt.getClaimAsString("given_name");
        }
        return name != null ? name : "Usuario";
    }

    /**
     * Determina el rol del usuario basado en su email
     */
    private String getUserRole(String userEmail, String userName) {
        if (userEmail == null || userEmail.isEmpty()) {
            return "ROLE_USER"; // Rol por defecto
        }
        
        // Buscar usuario específico
        String specificRole = USER_TO_ROLE_MAPPING.get(userEmail);
        if (specificRole != null) {
            return specificRole;
        }
        
        // Buscar por dominio
        for (Map.Entry<String, String> entry : DOMAIN_TO_ROLE_MAPPING.entrySet()) {
            if (userEmail.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // Rol por defecto para usuarios autenticados
        return "ROLE_USER";
    }

    /**
     * Obtiene el rol más alto basado en jerarquía
     */
    public static String getHighestRole(Collection<GrantedAuthority> authorities) {
        Set<String> roles = authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
            
        if (roles.contains("ROLE_ADMIN")) return "ADMINISTRADOR";
        if (roles.contains("ROLE_MANAGER")) return "GESTOR";
        if (roles.contains("ROLE_USER")) return "USUARIO";
        if (roles.contains("ROLE_READER")) return "LECTOR";
        
        return "SIN_PERMISOS";
    }

    /**
     * Verifica si el usuario tiene un rol específico
     */
    public static boolean hasRole(Collection<GrantedAuthority> authorities, String role) {
        return authorities.stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role.toUpperCase()));
    }
}
