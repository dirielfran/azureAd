package com.example.apiprotegida.controller;

import com.example.apiprotegida.model.Usuario;
import com.example.apiprotegida.repository.UsuarioRepository;
import com.example.apiprotegida.security.JWTTokenProvider;
import com.example.apiprotegida.service.ConfiguracionService;
import com.example.apiprotegida.service.PasswordResetService;
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

    @Autowired
    private PasswordResetService passwordResetService;

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
     * Endpoint para solicitar recuperación de contraseña
     * @param request Request con el email del usuario
     * @return Respuesta indicando que se procesó la solicitud
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        log.info("📧 [LocalAuth] Solicitud de recuperación de contraseña para: {}", request.getEmail());

        try {
            // Validar email
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El email es requerido"));
            }

            // Procesar solicitud (siempre retorna éxito por seguridad)
            passwordResetService.solicitarRecuperacion(request.getEmail().trim());

            // Por seguridad, siempre retornar el mismo mensaje
            return ResponseEntity.ok(Map.of(
                    "message", "Si el email existe en nuestro sistema, recibirás un enlace de recuperación"
            ));

        } catch (Exception e) {
            log.error("❌ [LocalAuth] Error al procesar solicitud de recuperación: {}", e.getMessage(), e);
            // Por seguridad, siempre retornar éxito
            return ResponseEntity.ok(Map.of(
                    "message", "Si el email existe en nuestro sistema, recibirás un enlace de recuperación"
            ));
        }
    }

    /**
     * Endpoint para resetear contraseña con token
     * @param request Request con token y nueva contraseña
     * @return Respuesta indicando si el reseteo fue exitoso
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        log.info("🔄 [LocalAuth] Intento de reseteo de contraseña");

        try {
            // Validar campos
            if (request.getToken() == null || request.getToken().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El token es requerido"));
            }

            if (request.getNewPassword() == null || request.getNewPassword().trim().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "La contraseña debe tener al menos 6 caracteres"));
            }

            // Validar token primero
            if (!passwordResetService.validarToken(request.getToken())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Token inválido o expirado"));
            }

            // Procesar reseteo
            boolean exito = passwordResetService.resetearPassword(
                    request.getToken().trim(),
                    request.getNewPassword().trim()
            );

            if (exito) {
                return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No se pudo actualizar la contraseña. Verifica que el token sea válido."));
            }

        } catch (Exception e) {
            log.error("❌ [LocalAuth] Error al resetear contraseña: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al procesar la solicitud"));
        }
    }

    /**
     * Endpoint para validar si un token de recuperación es válido
     * @param request Request con el token
     * @return true si el token es válido
     */
    @PostMapping("/validate-reset-token")
    public ResponseEntity<Map<String, Boolean>> validateResetToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("valid", false));
        }

        boolean isValid = passwordResetService.validarToken(token.trim());
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

    /**
     * DTO para solicitud de recuperación de contraseña
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForgotPasswordRequest {
        private String email;
    }

    /**
     * DTO para reseteo de contraseña
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResetPasswordRequest {
        private String token;
        private String newPassword;
    }
}

