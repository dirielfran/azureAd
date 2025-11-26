package com.example.apiprotegida.service;

import com.example.apiprotegida.model.PasswordResetToken;
import com.example.apiprotegida.model.Usuario;
import com.example.apiprotegida.repository.PasswordResetTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * Servicio para manejo de recuperación de contraseña
 */
@Service
@Slf4j
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EmailService emailService;

    @Value("${password.reset.token.expiration.hours:1}")
    private int tokenExpirationHours;

    @Value("${password.reset.rate.limit.max:3}")
    private int maxRequestsPerHour;

    /**
     * Genera y envía token de recuperación de contraseña
     * @param email Email del usuario que solicita recuperación
     * @return true si se procesó la solicitud (por seguridad, siempre retorna true)
     */
    @Transactional
    public boolean solicitarRecuperacion(String email) {
        log.info("🔐 [PasswordReset] Solicitud de recuperación para: {}", email);

        // Buscar usuario por email
        Usuario usuario = usuarioService.obtenerUsuarioPorEmail(email);
        
        // Por seguridad, siempre retornar éxito (no revelar si el email existe)
        if (usuario == null) {
            log.warn("⚠️ [PasswordReset] Usuario no encontrado: {} (no se revela al cliente)", email);
            return true; // Por seguridad, no revelar si el email existe
        }

        // Verificar que el usuario tenga contraseña (solo usuarios locales)
        if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
            log.warn("⚠️ [PasswordReset] Usuario sin contraseña local: {} (probablemente solo Azure AD)", email);
            return true; // Por seguridad, no revelar información
        }

        // Verificar rate limiting (máximo X solicitudes por hora)
        LocalDateTime unaHoraAtras = LocalDateTime.now().minusHours(1);
        long solicitudesRecientes = tokenRepository.contarTokensRecientes(usuario, unaHoraAtras);
        
        if (solicitudesRecientes >= maxRequestsPerHour) {
            log.warn("⚠️ [PasswordReset] Rate limit excedido para: {} ({} solicitudes en la última hora)", 
                    email, solicitudesRecientes);
            return true; // Por seguridad, no revelar rate limiting
        }

        // Invalidar tokens anteriores del usuario
        tokenRepository.invalidarTokensDelUsuario(usuario);

        // Generar nuevo token
        String token = generarTokenSeguro();
        LocalDateTime fechaExpiracion = LocalDateTime.now().plusHours(tokenExpirationHours);

        // Guardar token
        PasswordResetToken resetToken = new PasswordResetToken(token, usuario, fechaExpiracion);
        tokenRepository.save(resetToken);

        log.info("✅ [PasswordReset] Token generado para: {} (expira en {} horas)", email, tokenExpirationHours);

        // Enviar email
        emailService.enviarEmailRecuperacion(email, token, usuario.getNombre());

        return true;
    }

    /**
     * Valida y procesa el reseteo de contraseña
     * @param token Token de recuperación
     * @param nuevaPassword Nueva contraseña
     * @return true si el reseteo fue exitoso
     */
    @Transactional
    public boolean resetearPassword(String token, String nuevaPassword) {
        log.info("🔄 [PasswordReset] Intento de reseteo con token: {}...", token.substring(0, Math.min(8, token.length())));

        // Buscar token
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isEmpty()) {
            log.warn("❌ [PasswordReset] Token no encontrado");
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();

        // Validar token
        if (!resetToken.esValido()) {
            log.warn("❌ [PasswordReset] Token inválido o expirado para usuario: {}", resetToken.getUsuario().getEmail());
            return false;
        }

        // Validar nueva contraseña
        if (nuevaPassword == null || nuevaPassword.trim().length() < 6) {
            log.warn("❌ [PasswordReset] Nueva contraseña inválida (mínimo 6 caracteres)");
            return false;
        }

        // Actualizar contraseña
        try {
            usuarioService.actualizarPassword(resetToken.getUsuario(), nuevaPassword);
        } catch (IllegalArgumentException e) {
            log.error("❌ [PasswordReset] Error al actualizar contraseña: {}", e.getMessage());
            return false;
        }

        // Marcar token como usado
        resetToken.setUsado(true);
        tokenRepository.save(resetToken);

        log.info("✅ [PasswordReset] Contraseña actualizada exitosamente para: {}", resetToken.getUsuario().getEmail());

        return true;
    }

    /**
     * Valida si un token es válido (sin usarlo)
     * @param token Token a validar
     * @return true si el token es válido
     */
    @Transactional(readOnly = true)
    public boolean validarToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isEmpty()) {
            return false;
        }

        return tokenOpt.get().esValido();
    }

    /**
     * Genera un token seguro aleatorio
     * @return Token de 32 caracteres en Base64
     */
    private String generarTokenSeguro() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[24]; // 24 bytes = 32 caracteres en Base64
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Limpia tokens expirados (puede ser llamado periódicamente)
     */
    @Transactional
    public void limpiarTokensExpirados() {
        log.info("🧹 [PasswordReset] Limpiando tokens expirados...");
        LocalDateTime ahora = LocalDateTime.now();
        tokenRepository.eliminarTokensExpirados(ahora);
        log.info("✅ [PasswordReset] Limpieza completada");
    }
}







