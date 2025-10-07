package com.example.apiprotegida.controller;

import com.example.apiprotegida.model.ConfiguracionSistema;
import com.example.apiprotegida.service.ConfiguracionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar configuraciones del sistema
 * Solo accesible por administradores
 */
@RestController
@RequestMapping("/config")
@CrossOrigin(origins = {"http://localhost:4200", "https://localhost:4200"})
@Slf4j
public class ConfiguracionController {
    
    @Autowired
    private ConfiguracionService configuracionService;
    


    /**
     * Endpoint con token de administrador personalizado
     */
    @PostMapping("/auth/config/admin")
    public ResponseEntity<Map<String, Object>> adminConfig(@RequestHeader("X-Admin-Token") String adminToken,
                                                          @RequestBody Map<String, Object> config) {
        // Verificar token de administrador
        String validAdminToken = System.getenv("ADMIN_TOKEN");
        if (validAdminToken == null) {
            validAdminToken = "ADMIN_SECRET_TOKEN_2024"; // Token por defecto para desarrollo
        }
        
        if (!validAdminToken.equals(adminToken)) {
            log.warn("🚨 Intento de acceso no autorizado con token inválido");
            return ResponseEntity.status(401).body(Map.of("error", "Token de administrador inválido"));
        }
        
        log.info("🔒 [ADMIN] Configuración cambiada desde endpoint de administrador");
        
        Boolean azureEnabled = (Boolean) config.get("azureEnabled");
        Boolean jwtLocalEnabled = (Boolean) config.get("jwtLocalEnabled");
        
        // Validación de seguridad: al menos un método de autenticación debe estar activo
        boolean currentAzureEnabled = configuracionService.esAzureAdHabilitado();
        boolean currentJwtLocalEnabled = configuracionService.esJwtLocalHabilitado();
        
        // Determinar el estado final después de los cambios
        boolean finalAzureEnabled = (azureEnabled != null) ? azureEnabled : currentAzureEnabled;
        boolean finalJwtLocalEnabled = (jwtLocalEnabled != null) ? jwtLocalEnabled : currentJwtLocalEnabled;
        
        // Validar que al menos un método esté habilitado
        if (!finalAzureEnabled && !finalJwtLocalEnabled) {
            log.error("🚨 [SECURITY] Intento de deshabilitar todos los métodos de autenticación - OPERACIÓN RECHAZADA");
            return ResponseEntity.status(400).body(Map.of(
                "error", "No se puede deshabilitar todos los métodos de autenticación",
                "mensaje", "Al menos un método de autenticación debe estar activo para mantener el acceso al sistema",
                "azureAdHabilitado", currentAzureEnabled,
                "jwtLocalHabilitado", currentJwtLocalEnabled
            ));
        }
        
        // Aplicar cambios solo si la validación pasa
        try {
            if (azureEnabled != null) {
                configuracionService.establecerAzureAdHabilitado(azureEnabled);
                log.info("🔧 [CONFIG] Azure AD {} por administrador", azureEnabled ? "habilitado" : "deshabilitado");
            }
            
            if (jwtLocalEnabled != null) {
                configuracionService.establecerJwtLocalHabilitado(jwtLocalEnabled);
                log.info("🔧 [CONFIG] JWT Local {} por administrador", jwtLocalEnabled ? "habilitado" : "deshabilitado");
            }
        } catch (IllegalStateException e) {
            log.error("🚨 [SECURITY] Error de validación en el servicio: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of(
                "error", "Operación no permitida por seguridad",
                "mensaje", e.getMessage(),
                "azureAdHabilitado", configuracionService.esAzureAdHabilitado(),
                "jwtLocalHabilitado", configuracionService.esJwtLocalHabilitado()
            ));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Configuración actualizada exitosamente");
        response.put("azureAdHabilitado", configuracionService.esAzureAdHabilitado());
        response.put("jwtLocalHabilitado", configuracionService.esJwtLocalHabilitado());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene el estado actual de los métodos de autenticación
     */
    @GetMapping("/auth/status")
    public ResponseEntity<Map<String, Object>> obtenerEstadoAutenticacion() {
        log.info("📊 Consultando estado de métodos de autenticación");
        
        Map<String, Object> status = new HashMap<>();
        status.put("azureAdHabilitado", configuracionService.esAzureAdHabilitado());
        status.put("jwtLocalHabilitado", configuracionService.esJwtLocalHabilitado());
        status.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(status);
    }
    
    
    /**
     * Obtiene todas las configuraciones de autenticación
     * Solo administradores
     */
    @GetMapping("/auth/all")
    @PreAuthorize("hasAnyAuthority('SCOPE_access_as_user', 'ADMIN', 'USUARIOS_LEER')")
    public ResponseEntity<List<ConfiguracionSistema>> obtenerConfiguracionesAutenticacion() {
        log.info("📋 Consultando todas las configuraciones de autenticación");
        return ResponseEntity.ok(configuracionService.obtenerConfiguracionesAutenticacion());
    }
    
    /**
     * Obtiene todas las configuraciones activas del sistema
     * Solo administradores
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('SCOPE_access_as_user', 'ADMIN', 'USUARIOS_LEER')")
    public ResponseEntity<List<ConfiguracionSistema>> obtenerTodasLasConfiguraciones() {
        log.info("📋 Consultando todas las configuraciones del sistema");
        return ResponseEntity.ok(configuracionService.obtenerTodasActivas());
    }
    
    /**
     * Actualiza el valor de una configuración específica
     * Solo administradores
     */
    @PutMapping("/{clave}")
    @PreAuthorize("hasAnyAuthority('SCOPE_access_as_user', 'ADMIN')")
    public ResponseEntity<ConfiguracionSistema> actualizarConfiguracion(
            @PathVariable String clave,
            @RequestBody Map<String, String> request) {
        
        String nuevoValor = request.get("valor");
        if (nuevoValor == null) {
            return ResponseEntity.badRequest().build();
        }
        
        log.info("🔧 [ADMIN] Actualizando configuración: {} = {}", clave, nuevoValor);
        
        try {
            ConfiguracionSistema config = configuracionService.actualizarValor(clave, nuevoValor);
            return ResponseEntity.ok(config);
        } catch (IllegalArgumentException e) {
            log.error("❌ Error al actualizar configuración: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}


