package com.example.apiprotegida.config;

import com.example.apiprotegida.model.Perfil;
import com.example.apiprotegida.model.Permiso;
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
      Collection<GrantedAuthority> authorities = new ArrayList<>();

      // Extraer grupos de Azure AD del token
      List<String> azureGroups = getAzureGroups(jwt);

      String userEmail = getUserEmail(jwt);
      // Agregar grupos como authorities para que puedan ser procesados por el AuthorizationService
      for (String groupId : azureGroups) {
        authorities.add(new SimpleGrantedAuthority("GROUP_" + groupId));

        // Buscar perfil asociado al grupo y agregar como rol
        try {
          Optional<Perfil> perfil = perfilService.obtenerPerfilPorAzureGroupIdConPermisos(groupId);
          if (perfil.isPresent()) {
            String roleName = getRoleName(perfil.get().getAzureGroupName());
            authorities.add(new SimpleGrantedAuthority(roleName));

            Set<Permiso> permisos = perfil.get().getPermisos();
            permisos.stream().filter(Permiso::getActivo).forEach( permiso -> authorities.add(new SimpleGrantedAuthority(permiso.getNombre())));
          } else {
            System.out.println("❌ [AzureAdGroupsJwtConverter] NO se encontró perfil para grupo ID: " + groupId);
          }
        } catch (Exception e) {
          System.out.println("⚠️ [AzureAdGroupsJwtConverter] Error al buscar perfil para grupo " + groupId + ": " + e.getMessage());
          e.printStackTrace();
        }
      }

      return authorities;
    }

  private String getRoleName(String group){
    return "ROLE_"+group.toUpperCase().replace(" ", "_");
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
