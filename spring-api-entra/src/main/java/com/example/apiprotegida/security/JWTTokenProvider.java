package com.example.apiprotegida.security;

import com.example.apiprotegida.model.Usuario;
import com.example.apiprotegida.model.Perfil;
import com.example.apiprotegida.model.Permiso;
import com.example.apiprotegida.repository.UsuarioRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.apiprotegida.security.SecurityConstant.*;
import static java.util.Arrays.stream;

/**
 * Proveedor de tokens JWT para autenticación local
 * Compatible con el sistema de permisos existente
 */
@Component
@Slf4j
public class JWTTokenProvider {
    
    @Value("${jwt.secret:defaultSecretKeyForJWTTokenGeneration123456789}")
    private String secret;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Genera un token JWT para un usuario
     * @param usuario El usuario para el cual generar el token
     * @return Token JWT con prefijo Bearer
     */
    public String generateJwtToken(Usuario usuario) {
        log.info("🔑 [JWTTokenProvider] Generando token para usuario: {}", usuario.getEmail());
        
        // Recargar el usuario con sus perfiles y permisos desde la base de datos
        Usuario usuarioConPerfiles = usuarioRepository.findByEmailWithPerfiles(usuario.getEmail())
                .orElse(usuario);
        
        // Obtener perfiles del usuario
        Set<Perfil> perfiles = usuarioConPerfiles.getPerfiles();
        String nombrePerfil = perfiles.isEmpty() ? "Usuario Básico" : 
                              perfiles.iterator().next().getNombre();
        
        log.info("📋 [JWTTokenProvider] Perfiles del usuario: {}", 
                perfiles.stream().map(Perfil::getNombre).collect(Collectors.joining(", ")));
        
        // Construir lista de authorities (roles + permisos)
        List<String> authorities = new ArrayList<>();
        
        // Siempre agregar rol USER para que Spring Security lo reconozca
        authorities.add("ROLE_USER");
        
        // Agregar permisos de todos los perfiles del usuario
        for (Perfil perfil : perfiles) {
            for (Permiso permiso : perfil.getPermisos()) {
                if (permiso.getActivo()) {
                    authorities.add(permiso.getCodigo());
                }
            }
        }
        
        // Si el usuario es admin (tiene perfil de Administrador), agregar ROLE_ADMIN
        boolean isAdmin = perfiles.stream()
                .anyMatch(p -> p.getNombre().equalsIgnoreCase("Administrador") || 
                              p.getNombre().equalsIgnoreCase("Admin"));
        if (isAdmin) {
            authorities.add("ROLE_ADMIN");
        }
        
        // Si el usuario es manager/gestor, agregar ROLE_MANAGER
        boolean isManager = perfiles.stream()
                .anyMatch(p -> p.getNombre().equalsIgnoreCase("Gestor") || 
                              p.getNombre().equalsIgnoreCase("Manager"));
        if (isManager) {
            authorities.add("ROLE_MANAGER");
        }
        
        log.info("🔑 [JWTTokenProvider] Authorities generadas: {}", authorities);
        
        return TOKEN_PREFIX + JWT.create()
                .withIssuer(API_TYC)
                .withSubject(usuario.getEmail())
                .withClaim("perfil", nombrePerfil)
                .withArrayClaim(AUTHORITIES, authorities.toArray(new String[0]))
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(secret.getBytes()));
    }

    /**
     * Extrae las autoridades del token JWT
     * @param token El token JWT
     * @return Lista de autoridades
     */
    public List<GrantedAuthority> getAuthorities(String token) {
        String[] claims = getClaimsFromToken(token);
        return stream(claims)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * Crea un objeto de autenticación
     * @param email Email del usuario
     * @param authorities Lista de autoridades
     * @param request Petición HTTP
     * @return Objeto de autenticación
     */
    public Authentication getAuthentication(String email, List<GrantedAuthority> authorities, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken userPasswordAuthToken
                = new UsernamePasswordAuthenticationToken(email, null, authorities);

        userPasswordAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return userPasswordAuthToken;
    }

    /**
     * Extrae el sujeto (email) del token
     * @param token El token JWT
     * @return Email del usuario
     */
    public String getSubject(String token) {
        JWTVerifier verifier = getJWTVerifier();
        return verifier.verify(token).getSubject();
    }

    /**
     * Valida si un token JWT es válido
     * @param token El token a validar
     * @return true si es válido, false en caso contrario
     */
    public boolean isTokenValid(String token) {
        JWTVerifier verifier = getJWTVerifier();
        boolean isValid = false;
        try {
            DecodedJWT jwtClaims = verifier.verify(token);
            Date expiration = jwtClaims.getExpiresAt();
            isValid = !isTokenExpired(expiration);
        } catch (TokenExpiredException e) {
            log.error("El token se encuentra expirado: {}", e.getMessage());
        } catch (JWTVerificationException e) {
            log.error("Hubo un error al verificar el token: {}", e.getMessage());
        }
        return isValid;
    }

    /**
     * Extrae el perfil del token
     * @param token El token JWT
     * @return Nombre del perfil
     */
    public String getPerfilFromToken(String token) {
        return getJWTVerifier().verify(token).getClaim("perfil").asString();
    }

    /**
     * Verifica si un token está expirado
     * @param expiration Fecha de expiración
     * @return true si está expirado
     */
    private boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

    /**
     * Extrae los claims de autoridades del token
     * @param token El token JWT
     * @return Array de autoridades
     */
    private String[] getClaimsFromToken(String token) {
        JWTVerifier verifier = getJWTVerifier();
        return verifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);
    }

    /**
     * Crea un verificador JWT
     * @return Verificador JWT configurado
     */
    private JWTVerifier getJWTVerifier() {
        JWTVerifier verifier;
        try {
            Algorithm algorithm = Algorithm.HMAC512(secret);
            verifier = JWT.require(algorithm).withIssuer(API_TYC).build();
        } catch (JWTVerificationException ex) {
            throw new JWTVerificationException(TOKEN_NO_SE_PUEDE_VERIFICAR);
        }
        return verifier;
    }
}
