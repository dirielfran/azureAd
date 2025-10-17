package com.example.apiprotegida.service;

import com.example.apiprotegida.model.Perfil;
import com.example.apiprotegida.model.Permiso;
import com.example.apiprotegida.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de autorización que maneja la lógica de permisos basada en grupos de Azure AD
 */
@Service
@Transactional(readOnly = true)
public class AuthorizationService {

    @Autowired
    private PerfilService perfilService;

    @Autowired
    private PermisoService permisoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtiene los permisos del usuario autenticado basado en sus grupos de Azure AD
     */
    public List<Permiso> obtenerPermisosUsuario(Authentication authentication) {
        List<String> azureGroupIds = extraerGruposAzureAD(authentication);
        
        List<Perfil> perfiles;
        if (azureGroupIds.isEmpty()) {
            System.out.println("⚠️ Usuario sin grupos de Azure AD, asignando perfil por defecto");
            // Asignar perfil por defecto para usuarios sin grupos
            perfiles = perfilService.obtenerPerfilesPorAzureGroupIds(List.of("default-user"));
        } else {
            perfiles = perfilService.obtenerPerfilesPorAzureGroupIds(azureGroupIds);
            
            // Si no se encontraron perfiles para los grupos, usar perfil por defecto
            if (perfiles.isEmpty()) {
                System.out.println("⚠️ No se encontraron perfiles para los grupos: " + azureGroupIds + ", asignando perfil por defecto");
                perfiles = perfilService.obtenerPerfilesPorAzureGroupIds(List.of("default-user"));
            }
        }
        
        System.out.println("✅ Perfiles asignados: " + perfiles.stream().map(Perfil::getNombre).collect(Collectors.toList()));
        
        return perfiles.stream()
            .flatMap(perfil -> perfil.getPermisos().stream())
            .filter(Permiso::getActivo)
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * Obtiene los códigos de permisos del usuario autenticado
     */
    public Set<String> obtenerCodigosPermisosUsuario(Authentication authentication) {
        return obtenerPermisosUsuario(authentication).stream()
            .map(Permiso::getCodigo)
            .collect(Collectors.toSet());
    }

    /**
     * Obtiene los perfiles del usuario autenticado basado en sus grupos de Azure AD
     */
    public List<Perfil> obtenerPerfilesUsuario(Authentication authentication) {
        List<String> azureGroupIds = extraerGruposAzureAD(authentication);
        
        List<Perfil> perfiles;
        if (azureGroupIds.isEmpty()) {
            // Asignar perfil por defecto para usuarios sin grupos
            perfiles = perfilService.obtenerPerfilesPorAzureGroupIds(List.of("default-user"));
        } else {
            perfiles = perfilService.obtenerPerfilesPorAzureGroupIds(azureGroupIds);
            
            // Si no se encontraron perfiles para los grupos, usar perfil por defecto
            if (perfiles.isEmpty()) {
                perfiles = perfilService.obtenerPerfilesPorAzureGroupIds(List.of("default-user"));
            }
        }

        return perfiles;
    }

    /**
     * Verifica si el usuario tiene un permiso específico
     */
    public boolean tienePermiso(Authentication authentication, String codigoPermiso) {
        Set<String> permisosUsuario = obtenerCodigosPermisosUsuario(authentication);
        return permisosUsuario.contains(codigoPermiso);
    }

    /**
     * Verifica si el usuario tiene alguno de los permisos especificados
     */
    public boolean tieneAlgunoDeEstosPermisos(Authentication authentication, String... codigosPermisos) {
        Set<String> permisosUsuario = obtenerCodigosPermisosUsuario(authentication);
        return Arrays.stream(codigosPermisos)
            .anyMatch(permisosUsuario::contains);
    }

    /**
     * Verifica si el usuario tiene todos los permisos especificados
     */
    public boolean tieneTodosLosPermisos(Authentication authentication, String... codigosPermisos) {
        Set<String> permisosUsuario = obtenerCodigosPermisosUsuario(authentication);
        return Arrays.stream(codigosPermisos)
            .allMatch(permisosUsuario::contains);
    }

    /**
     * Verifica si el usuario tiene permisos para un módulo específico
     */
    public boolean tienePermisoEnModulo(Authentication authentication, String modulo) {
        List<Permiso> permisosUsuario = obtenerPermisosUsuario(authentication);
        return permisosUsuario.stream()
            .anyMatch(permiso -> modulo.equals(permiso.getModulo()));
    }

    /**
     * Verifica si el usuario tiene permisos para una acción específica
     */
    public boolean tienePermisoParaAccion(Authentication authentication, String accion) {
        List<Permiso> permisosUsuario = obtenerPermisosUsuario(authentication);
        return permisosUsuario.stream()
            .anyMatch(permiso -> accion.equals(permiso.getAccion()));
    }

    /**
     * Verifica si el usuario tiene permisos para una acción en un módulo específico
     */
    public boolean tienePermisoEnModuloYAccion(Authentication authentication, String modulo, String accion) {
        List<Permiso> permisosUsuario = obtenerPermisosUsuario(authentication);
        return permisosUsuario.stream()
            .anyMatch(permiso -> modulo.equals(permiso.getModulo()) && accion.equals(permiso.getAccion()));
    }

    /**
     * Obtiene información completa del usuario para el frontend
     */
    public Map<String, Object> obtenerInformacionCompleteUsuario(Authentication authentication) {
        Map<String, Object> info = new HashMap<>();
        
        // Información básica del usuario
        info.put("email", obtenerEmailUsuario(authentication));
        info.put("nombre", obtenerNombreUsuario(authentication));
        info.put("grupos", extraerGruposAzureAD(authentication));
        
        // Perfiles y permisos
        List<Perfil> perfiles = obtenerPerfilesUsuario(authentication);
        List<Permiso> permisos = obtenerPermisosUsuario(authentication);
        
        info.put("perfiles", perfiles.stream()
            .map(perfil -> Map.of(
                "id", perfil.getId(),
                "nombre", perfil.getNombre(),
                "descripcion", perfil.getDescripcion() != null ? perfil.getDescripcion() : "",
                "azureGroupId", perfil.getAzureGroupId() != null ? perfil.getAzureGroupId() : "",
                "azureGroupName", perfil.getAzureGroupName() != null ? perfil.getAzureGroupName() : ""
            ))
            .collect(Collectors.toList()));
        
        info.put("permisos", permisos.stream()
            .map(permiso -> Map.of(
                "codigo", permiso.getCodigo(),
                "nombre", permiso.getNombre(),
                "modulo", permiso.getModulo(),
                "accion", permiso.getAccion(),
                "descripcion", permiso.getDescripcion() != null ? permiso.getDescripcion() : ""
            ))
            .collect(Collectors.toList()));
        
        // Códigos de permisos para validación rápida en el frontend
        info.put("codigosPermisos", permisos.stream()
            .map(Permiso::getCodigo)
            .collect(Collectors.toSet()));
        
        return info;
    }

    /**
     * Extrae los grupos de Azure AD del token JWT
     */
    private List<String> extraerGruposAzureAD(Authentication authentication) {
        System.out.println("🔍 Extrayendo grupos de Azure AD...");
        System.out.println("Authentication type: " + authentication.getClass().getSimpleName());
        System.out.println("Principal type: " + authentication.getPrincipal().getClass().getSimpleName());
        
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            System.out.println("📋 Claims del JWT:");
            jwt.getClaims().forEach((key, value) -> 
                System.out.println("  " + key + ": " + value));
            
            // Intentar obtener grupos del claim "groups"
            List<String> groups = jwt.getClaimAsStringList("groups");
            System.out.println("🏷️ Grupos del claim 'groups': " + groups);
            
            if (groups != null && !groups.isEmpty()) {
                System.out.println("✅ Grupos encontrados en claim 'groups': " + groups);
                return groups;
            }
            
            // Si no hay grupos en el claim, intentar extraer de roles/authorities
            System.out.println("🔍 Intentando extraer de authorities...");
            List<String> authGroups = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("GROUP_"))
                .map(auth -> auth.substring(6)) // Remover prefijo "GROUP_"
                .collect(Collectors.toList());
            
            System.out.println("🏷️ Grupos de authorities: " + authGroups);
            
            if (!authGroups.isEmpty()) {
                System.out.println("✅ Grupos encontrados en authorities: " + authGroups);
                return authGroups;
            }
        }
        
        System.out.println("❌ No se encontraron grupos en el token");
        return new ArrayList<>();
    }

    /**
     * Extrae el email del usuario del token JWT o autenticación local
     */
    private String obtenerEmailUsuario(Authentication authentication) {
        // Para autenticación Azure AD (JWT)
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String email = jwt.getClaimAsString("email");
            if (email == null) {
                email = jwt.getClaimAsString("preferred_username");
            }
            if (email == null) {
                email = jwt.getClaimAsString("upn");
            }
            return email != null ? email : "";
        }
        
        // Para autenticación local (String como principal)
        if (authentication.getPrincipal() instanceof String email) {
            return email;
        }
        
        return "";
    }

    /**
     * Extrae el nombre del usuario del token JWT o autenticación local
     */
    private String obtenerNombreUsuario(Authentication authentication) {
        // Para autenticación Azure AD (JWT)
        if (authentication.getPrincipal() instanceof Jwt jwt) {
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
        
        // Para autenticación local (String como principal)
        if (authentication.getPrincipal() instanceof String email) {
            try {
                return usuarioRepository.findByEmail(email)
                    .map(usuario -> usuario.getNombre() != null ? usuario.getNombre() : "Usuario")
                    .orElse("Usuario");
            } catch (Exception e) {
                System.out.println("⚠️ Error al obtener nombre del usuario desde BD: " + e.getMessage());
                return "Usuario";
            }
        }
        
        return "Usuario";
    }
}
