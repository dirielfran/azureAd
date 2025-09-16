package com.example.apiprotegida.config;

import com.example.apiprotegida.model.Perfil;
import com.example.apiprotegida.service.PerfilService;
import org.springframework.beans.factory.annotation.Autowired;
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
 * y convertirlos en authorities de Spring Security basado en perfiles de la base de datos
 */
@Component
public class AzureAdGroupsJwtConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Autowired
    private PerfilService perfilService;

    @Override
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
        System.out.println("🔧 [AzureAdGroupsJwtConverter] Iniciando conversión de JWT a authorities");
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        // Extraer información del usuario del token JWT
        String userEmail = getUserEmail(jwt);
        String userName = getUserName(jwt);
        
        System.out.println("👤 [AzureAdGroupsJwtConverter] Usuario autenticado: " + userName + " (" + userEmail + ")");
        System.out.println("🔑 [AzureAdGroupsJwtConverter] Token JWT recibido - Issuer: " + jwt.getIssuer());
        System.out.println("🔑 [AzureAdGroupsJwtConverter] Token JWT recibido - Audience: " + jwt.getAudience());
        
        // Extraer grupos de Azure AD del token
        List<String> azureGroups = getAzureGroups(jwt);
        System.out.println("🏢 [AzureAdGroupsJwtConverter] Grupos de Azure AD encontrados: " + azureGroups);
        
        // Agregar grupos como authorities para que puedan ser procesados por el AuthorizationService
        for (String groupId : azureGroups) {
            authorities.add(new SimpleGrantedAuthority("GROUP_" + groupId));
            System.out.println("➕ [AzureAdGroupsJwtConverter] Agregado GROUP_" + groupId + " como authority");
            
            // Buscar perfil asociado al grupo y agregar como rol
            try {
                System.out.println("🔍 [AzureAdGroupsJwtConverter] Buscando perfil para grupo ID: " + groupId);
                Optional<Perfil> perfil = perfilService.obtenerPerfilPorAzureGroupId(groupId);
                if (perfil.isPresent()) {
                    String perfilNombre = perfil.get().getNombre().toLowerCase();
                    String roleName;
                    
                    // Mapear nombres de perfiles a roles estándar
                    switch (perfilNombre) {
                        case "administrador":
                            roleName = "ROLE_ADMIN";
                            break;
                        case "gestor":
                            roleName = "ROLE_MANAGER";
                            break;
                        case "usuario":
                        case "usuario básico":
                            roleName = "ROLE_USER";
                            break;
                        case "lector":
                            roleName = "ROLE_READER";
                            break;
                        default:
                            roleName = "ROLE_" + perfil.get().getNombre().toUpperCase().replace(" ", "_");
                    }
                    
                    authorities.add(new SimpleGrantedAuthority(roleName));
                    System.out.println("✅ [AzureAdGroupsJwtConverter] Perfil encontrado: " + perfil.get().getNombre() + " -> " + roleName);
                } else {
                    System.out.println("❌ [AzureAdGroupsJwtConverter] NO se encontró perfil para grupo ID: " + groupId);
                }
            } catch (Exception e) {
                System.out.println("⚠️ [AzureAdGroupsJwtConverter] Error al buscar perfil para grupo " + groupId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Si no se encontraron grupos, asignar rol por defecto basado en email
        if (azureGroups.isEmpty()) {
            String defaultRole = getDefaultRole(userEmail);
            authorities.add(new SimpleGrantedAuthority(defaultRole));
            System.out.println("📝 [AzureAdGroupsJwtConverter] Rol por defecto asignado: " + userEmail + " -> " + defaultRole);
        }
        
        // Agregar scope por defecto para usuarios autenticados
        authorities.add(new SimpleGrantedAuthority("SCOPE_access_as_user"));
        System.out.println("➕ [AzureAdGroupsJwtConverter] Agregado SCOPE_access_as_user como authority");
        
        System.out.println("🔐 [AzureAdGroupsJwtConverter] Authorities finales generadas: " + authorities);
        System.out.println("✅ [AzureAdGroupsJwtConverter] Conversión de JWT completada exitosamente");
        return authorities;
    }

    /**
     * Extrae los grupos de Azure AD del token JWT
     */
    private List<String> getAzureGroups(Jwt jwt) {
        List<String> groups = new ArrayList<>();
        
        // Intentar obtener grupos del claim "groups"
        List<String> groupsClaim = jwt.getClaimAsStringList("groups");
        if (groupsClaim != null && !groupsClaim.isEmpty()) {
            groups.addAll(groupsClaim);
        }
        
        // También intentar con el claim "roles" (algunas configuraciones de Azure lo usan)
        List<String> rolesClaim = jwt.getClaimAsStringList("roles");
        if (rolesClaim != null && !rolesClaim.isEmpty()) {
            groups.addAll(rolesClaim);
        }
        
        return groups;
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
        if (name == null) {
            String firstName = jwt.getClaimAsString("given_name");
            String lastName = jwt.getClaimAsString("family_name");
            if (firstName != null && lastName != null) {
                name = firstName + " " + lastName;
            } else if (firstName != null) {
                name = firstName;
            }
        }
        return name != null ? name : "Usuario";
    }

    /**
     * Determina el rol por defecto del usuario basado en su email
     */
    private String getDefaultRole(String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) {
            return "ROLE_USER"; // Rol por defecto
        }
        
        // Mapeo básico por dominio (puedes personalizarlo)
        if (userEmail.contains("@admin.") || userEmail.contains("admin@")) {
            return "ROLE_ADMIN";
        }
        
        if (userEmail.contains("@manager.") || userEmail.contains("manager@")) {
            return "ROLE_MANAGER";
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
            .filter(auth -> auth.startsWith("ROLE_"))
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

    /**
     * Extrae los IDs de grupos de Azure AD de las authorities
     */
    public static List<String> extractAzureGroupIds(Collection<GrantedAuthority> authorities) {
        return authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .filter(auth -> auth.startsWith("GROUP_"))
            .map(auth -> auth.substring(6)) // Remover prefijo "GROUP_"
            .collect(Collectors.toList());
    }
}