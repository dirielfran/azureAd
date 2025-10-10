package com.example.apiprotegida.controller;

import com.example.apiprotegida.model.Usuario;
import com.example.apiprotegida.repository.UsuarioRepository;
import com.example.apiprotegida.security.JWTTokenProvider;
import com.example.apiprotegida.service.ConfiguracionService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para autenticación JWT local
 * Maneja el login con usuario y contraseña, generando tokens JWT locales
 */
@RestController
@RequestMapping("/auth/local")
@CrossOrigin(origins = {"http://localhost:4200", "https://localhost:4200"})
@Slf4j
public class LocalAuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTTokenProvider jwtTokenProvider;

    @Autowired
    private ConfiguracionService configuracionService;

    /**
     * Endpoint de login con JWT local
     * @param loginRequest Credenciales del usuario (email y password)
     * @return Token JWT si las credenciales son válidas
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        log.info("🔐 [LocalAuth] Intento de login para: {}", loginRequest.getEmail());

        // Verificar que JWT local esté habilitado
        if (!configuracionService.esJwtLocalHabilitado()) {
            log.warn("⚠️ [LocalAuth] Intento de login JWT pero JWT local está DESHABILITADO");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Autenticación JWT local deshabilitada"));
        }

        try {
            // Buscar usuario por email
            Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> {
                        log.warn("❌ [LocalAuth] Usuario no encontrado: {}", loginRequest.getEmail());
                        return new RuntimeException("Credenciales inválidas");
                    });

            // Verificar que el usuario esté activo
            if (usuario.getActivo() == null || !usuario.getActivo()) {
                log.warn("❌ [LocalAuth] Usuario inactivo: {}", loginRequest.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuario inactivo"));
            }

            // Verificar password
            if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
                log.warn("❌ [LocalAuth] Password incorrecto para: {}", loginRequest.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Credenciales inválidas"));
            }

            // Generar token JWT
            String token = jwtTokenProvider.generateJwtToken(usuario);
            log.info("✅ [LocalAuth] Login exitoso para: {}", loginRequest.getEmail());

            // Preparar respuesta
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setEmail(usuario.getEmail());
            response.setNombre(usuario.getNombre());
            response.setMessage("Login exitoso");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ [LocalAuth] Error en login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas"));
        }
    }

    /**
     * Endpoint para validar si un token JWT es válido
     * @param token Token JWT a validar
     * @return true si el token es válido, false en caso contrario
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        boolean isValid = jwtTokenProvider.isTokenValid(token);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    /**
     * DTO para request de login
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String email;
        private String password;
    }

    /**
     * DTO para response de login
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private String token;
        private String email;
        private String nombre;
        private String message;
    }
}

