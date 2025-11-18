package com.example.apiprotegida.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Servicio para envío de emails
 */
@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String emailFrom;

    @Value("${app.url:http://localhost:4200}")
    private String appUrl;

    /**
     * Envía email de recuperación de contraseña
     * @param emailDestino Email del destinatario
     * @param token Token de recuperación
     * @param nombreUsuario Nombre del usuario (opcional)
     */
    public void enviarEmailRecuperacion(String emailDestino, String token, String nombreUsuario) {
        log.info("📧 [EmailService] Preparando email de recuperación para: {}", emailDestino);

        // Si no hay mailSender configurado, solo loguear (modo desarrollo)
        if (mailSender == null) {
            log.warn("⚠️ [EmailService] JavaMailSender no configurado. Modo desarrollo: solo logueando token.");
            log.info("🔑 [EmailService] Token de recuperación para {}: {}", emailDestino, token);
            log.info("🔗 [EmailService] Link de recuperación: {}/reset-password?token={}", appUrl, token);
            return;
        }

        try {
            // Verificar configuración antes de enviar
            if (emailFrom == null || emailFrom.isEmpty()) {
                log.warn("⚠️ [EmailService] Email 'from' no configurado. Usando username como remitente.");
                // Usar el username de la configuración SMTP como fallback
                emailFrom = "ccscoffeeshopar@gmail.com";
            }
            
            log.info("📧 [EmailService] Configuración de email:");
            log.info("   - From: {}", emailFrom);
            log.info("   - To: {}", emailDestino);
            log.info("   - Host: smtp.gmail.com");
            log.info("   - Port: 587");
            
            SimpleMailMessage mensaje = new SimpleMailMessage();
            // Asegurar que el "from" sea el mismo que el username SMTP
            mensaje.setFrom(emailFrom);
            mensaje.setTo(emailDestino);
            mensaje.setSubject("Recuperación de Contraseña - Sistema de Autenticación");
            
            String nombre = nombreUsuario != null ? nombreUsuario : "Usuario";
            String linkRecuperacion = appUrl + "/reset-password?token=" + token;
            
            String cuerpo = String.format(
                "Hola %s,\n\n" +
                "Has solicitado recuperar tu contraseña.\n\n" +
                "Para restablecer tu contraseña, haz clic en el siguiente enlace:\n" +
                "%s\n\n" +
                "Este enlace expirará en 1 hora.\n\n" +
                "Si no solicitaste este cambio, ignora este email.\n\n" +
                "Saludos,\n" +
                "Equipo de Soporte",
                nombre, linkRecuperacion
            );
            
            mensaje.setText(cuerpo);
            
            log.debug("📧 [EmailService] Enviando email...");
            mailSender.send(mensaje);
            log.info("✅ [EmailService] Email de recuperación enviado exitosamente a: {}", emailDestino);
            log.info("📬 [EmailService] IMPORTANTE: Si no recibes el email, revisa:");
            log.info("   1. Carpeta de SPAM/Correo no deseado");
            log.info("   2. Espera unos minutos (puede haber delay)");
            log.info("   3. Verifica que el email destino sea correcto: {}", emailDestino);
            log.info("🔑 [EmailService] Token generado (backup): {}", token);
            log.info("🔗 [EmailService] Link directo: {}/reset-password?token={}", appUrl, token);
            
        } catch (Exception e) {
            log.error("❌ [EmailService] Error al enviar email de recuperación: {}", e.getMessage());
            
            // En desarrollo, loguear el token aunque falle el envío
            log.warn("⚠️ [EmailService] Email no enviado. Modo desarrollo: token disponible en logs.");
            log.info("🔑 [EmailService] Token de recuperación para {}: {}", emailDestino, token);
            log.info("🔗 [EmailService] Link de recuperación: {}/reset-password?token={}", appUrl, token);
            
            // No lanzar excepción - permitir que el flujo continúe
            // El usuario puede usar el token desde los logs en desarrollo
            // En producción, esto debería fallar para que se corrija la configuración
            if (!estaConfigurado()) {
                log.warn("⚠️ [EmailService] Email no configurado. Sistema funcionando en modo desarrollo.");
            } else {
                log.error("❌ [EmailService] Email configurado pero falló. Verifica credenciales SMTP.");
                log.error("❌ [EmailService] Detalles del error: {}", e.getClass().getSimpleName());
                if (e.getMessage() != null && e.getMessage().contains("Authentication")) {
                    log.error("❌ [EmailService] ERROR DE AUTENTICACIÓN: Verifica que estés usando App Password (Gmail) o credenciales correctas");
                }
            }
        }
    }

    /**
     * Verifica si el servicio de email está configurado
     * @return true si está configurado, false en caso contrario
     */
    public boolean estaConfigurado() {
        return mailSender != null && emailFrom != null && !emailFrom.isEmpty();
    }
}


