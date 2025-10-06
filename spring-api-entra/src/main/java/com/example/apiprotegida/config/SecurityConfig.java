package com.example.apiprotegida.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.example.apiprotegida.security.filter.DualAuthenticationFilter;

import java.util.Arrays;

/**
 * Configuración de seguridad para la API protegida
 * 
 * Esta clase configura:
 * - Autenticación JWT con Microsoft Entra ID
 * - CORS para permitir requests desde Angular
 * - Autorización basada en scopes
 * - Endpoints públicos y protegidos
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Autowired
    private AzureAdGroupsJwtConverter azureAdGroupsJwtConverter;

    @Autowired
    private DualAuthenticationFilter dualAuthenticationFilter;

    /**
     * Configuración principal de seguridad
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitar CSRF para APIs REST
            .csrf(csrf -> csrf.disable())
            
            // Configurar CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configurar autorización de requests
            .authorizeHttpRequests(authz -> authz
                // Endpoints públicos
                .requestMatchers(
                    "/actuator/**",
                    "/h2-console/**",
                    "/public/**",
                    "/auth/info",
                    "/auth/login"
                ).permitAll()
                
                // Todos los demás endpoints requieren autenticación
                // La autorización específica se maneja con @PreAuthorize en cada método
                .anyRequest().authenticated()
            )
            
            // Configurar OAuth2 Resource Server con JWT (para Azure AD)
            .oauth2ResourceServer(oauth2 -> oauth2
                .bearerTokenResolver(customBearerTokenResolver())
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            
            // Agregar filtro dual (maneja tanto Azure AD como JWT local)
            // Debe estar ANTES del BearerTokenAuthenticationFilter para procesar JWT locales
            .addFilterBefore(dualAuthenticationFilter, org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter.class)
            
            // Configurar sesiones como stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    /**
     * Configuración de CORS para permitir requests desde Angular
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Orígenes permitidos (Angular dev server)
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Permitir envío de cookies y headers de autorización
        configuration.setAllowCredentials(true);
        
        // Headers expuestos al cliente
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        
        // Aplicar configuración a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * Converter para extraer authorities de los claims del JWT
     * Ahora incluye soporte para grupos de Azure AD
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        
        // Usar nuestro convertidor personalizado que maneja grupos de Azure AD
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(azureAdGroupsJwtConverter);
        
        return jwtAuthenticationConverter;
    }

    /**
     * Decoder para JWT tokens de Microsoft Entra ID
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
    }

    /**
     * Encoder de contraseñas para usuarios locales
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * BearerTokenResolver personalizado que ignora tokens JWT locales ya procesados
     */
    @Bean
    public BearerTokenResolver customBearerTokenResolver() {
        DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();
        
        return new BearerTokenResolver() {
            @Override
            public String resolve(HttpServletRequest request) {
                // Si ya se procesó un token JWT local, no resolver el token aquí
                if (request.getAttribute("JWT_LOCAL_PROCESSED") != null) {
                    return null;
                }
                
                // Si ya hay autenticación establecida (por el filtro dual), no procesar
                if (SecurityContextHolder.getContext().getAuthentication() != null && 
                    SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
                    return null;
                }
                
                // De lo contrario, usar el resolver por defecto para tokens de Azure AD
                return resolver.resolve(request);
            }
        };
    }
}
