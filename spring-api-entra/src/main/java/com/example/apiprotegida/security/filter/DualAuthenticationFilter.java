package com.example.apiprotegida.security.filter;

import com.example.apiprotegida.security.JWTTokenProvider;
import com.example.apiprotegida.service.ConfiguracionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.example.apiprotegida.security.SecurityConstant.TOKEN_PREFIX;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * Filtro dual que maneja tanto autenticación Azure AD como JWT local
 * Determina el tipo de autenticación basado en el token y delega al filtro apropiado
 */
@Component
@Slf4j
public class DualAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JWTTokenProvider jwtTokenProvider;
    
    @Autowired
    private ConfiguracionService configuracionService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        log.debug("Procesando petición con filtro dual: {} {}", request.getMethod(), request.getRequestURI());
        
        // Solo procesar si no hay autenticación previa
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String authorizationHeader = request.getHeader(AUTHORIZATION);
            
            if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
                String token = authorizationHeader.substring(TOKEN_PREFIX.length());
                
                // Determinar si es un token JWT local o de Azure AD
                if (isLocalJwtToken(token)) {
                    // Verificar si JWT local está habilitado
                    if (!configuracionService.esJwtLocalHabilitado()) {
                        log.warn("⚠️ Token JWT local detectado pero JWT local está DESHABILITADO en la configuración");
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("{\"error\":\"Autenticación JWT local deshabilitada\"}");
                        response.setContentType("application/json");
                        return;
                    }
                    
                    log.debug("Token JWT local detectado, procesando...");
                    processLocalJwtToken(token, request);
                    
                    // Marcar que ya procesamos este token para que BearerTokenAuthenticationFilter lo ignore
                    request.setAttribute("JWT_LOCAL_PROCESSED", true);
                } else {
                    // Verificar si Azure AD está habilitado
                    if (!configuracionService.esAzureAdHabilitado()) {
                        log.warn("⚠️ Token Azure AD detectado pero Azure AD está DESHABILITADO en la configuración");
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("{\"error\":\"Autenticación Azure AD deshabilitada\"}");
                        response.setContentType("application/json");
                        return;
                    }
                    
                    log.debug("Token Azure AD detectado, delegando a OAuth2 Resource Server...");
                    // No hacer nada aquí, dejar que el OAuth2 Resource Server procese el token de Azure AD
                }
            } else {
                log.debug("No se encontró token de autorización");
            }
        } else {
            log.debug("Ya existe autenticación en el contexto: {}", 
                SecurityContextHolder.getContext().getAuthentication().getName());
        }
        
        // Siempre continuar con la cadena de filtros para que OAuth2 Resource Server pueda procesar tokens de Azure AD
        filterChain.doFilter(request, response);
    }

    /**
     * Determina si un token es JWT local o de Azure AD
     * @param token El token a analizar
     * @return true si es JWT local, false si es de Azure AD
     */
    private boolean isLocalJwtToken(String token) {
        try {
            // Intentar validar como token JWT local
            return jwtTokenProvider.isTokenValid(token);
        } catch (Exception e) {
            log.debug("Token no es JWT local: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Procesa un token JWT local
     * @param token El token JWT local
     * @param request La petición HTTP
     */
    private void processLocalJwtToken(String token, HttpServletRequest request) {
        try {
            if (jwtTokenProvider.isTokenValid(token)) {
                String subject = jwtTokenProvider.getSubject(token);
                var authorities = jwtTokenProvider.getAuthorities(token);
                
                log.debug("Token JWT local válido para usuario: {}", subject);
                log.debug("Autoridades encontradas: {}", authorities);
                
                Authentication authentication = jwtTokenProvider.getAuthentication(subject, authorities, request);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("Autenticación JWT local establecida en el contexto de seguridad");
            } else {
                log.debug("Token JWT local inválido");
                SecurityContextHolder.clearContext();
            }
        } catch (Exception e) {
            log.error("Error procesando token JWT local: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }
}
