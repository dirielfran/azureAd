package com.example.apiprotegida.service;

import com.example.apiprotegida.model.ConfiguracionSistema;
import com.example.apiprotegida.repository.ConfiguracionSistemaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar configuraciones del sistema
 */
@Service
@Slf4j
public class ConfiguracionService {

    @Autowired
    private ConfiguracionSistemaRepository configuracionRepository;

    // Claves de configuración
    public static final String AUTH_AZURE_AD_ENABLED = "auth.azure.enabled";
    public static final String AUTH_JWT_LOCAL_ENABLED = "auth.jwt.local.enabled";
    public static final String AUTH_REQUIRE_MFA = "auth.require.mfa";
    public static final String AUTH_SESSION_TIMEOUT = "auth.session.timeout";

    /**
     * Obtiene una configuración por su clave (con caché)
     */
    @Cacheable(value = "configuracion", key = "#clave")
    @Transactional(readOnly = true)
    public Optional<ConfiguracionSistema> obtenerPorClave(String clave) {
        log.debug("Obteniendo configuración: {}", clave);
        return configuracionRepository.findByClave(clave);
    }

    /**
     * Obtiene el valor de una configuración como String
     */
    @Transactional(readOnly = true)
    public String obtenerValor(String clave, String valorPorDefecto) {
        return obtenerPorClave(clave)
                .map(ConfiguracionSistema::getValor)
                .orElse(valorPorDefecto);
    }

    /**
     * Obtiene el valor de una configuración como Boolean
     */
    @Transactional(readOnly = true)
    public Boolean obtenerValorBoolean(String clave, Boolean valorPorDefecto) {
        return obtenerPorClave(clave)
                .map(ConfiguracionSistema::getValorBoolean)
                .orElse(valorPorDefecto);
    }

    /**
     * Verifica si Azure AD está habilitado
     */
    @Transactional(readOnly = true)
    public boolean esAzureAdHabilitado() {
        Boolean habilitado = obtenerValorBoolean(AUTH_AZURE_AD_ENABLED, true);
        log.debug("Azure AD habilitado: {}", habilitado);
        return habilitado;
    }

    /**
     * Verifica si JWT local está habilitado
     */
    @Transactional(readOnly = true)
    public boolean esJwtLocalHabilitado() {
        Boolean habilitado = obtenerValorBoolean(AUTH_JWT_LOCAL_ENABLED, true);
        log.debug("JWT Local habilitado: {}", habilitado);
        return habilitado;
    }

    /**
     * Actualiza el valor de una configuración
     */
    @CacheEvict(value = "configuracion", key = "#clave")
    @Transactional
    public ConfiguracionSistema actualizarValor(String clave, String nuevoValor) {
        log.info("Actualizando configuración: {} = {}", clave, nuevoValor);

        ConfiguracionSistema config = configuracionRepository.findByClave(clave)
                .orElseThrow(() -> new IllegalArgumentException("Configuración no encontrada: " + clave));

        config.setValor(nuevoValor);
        return configuracionRepository.save(config);
    }

    /**
     * Habilita o deshabilita Azure AD con validación de seguridad
     */
    @CacheEvict(value = "configuracion", key = "T(com.example.apiprotegida.service.ConfiguracionService).AUTH_AZURE_AD_ENABLED")
    @Transactional
    public void establecerAzureAdHabilitado(boolean habilitado, Boolean jwtLocalEnabledNuevo) {
        log.info("🔧 Cambiando estado de Azure AD a: {}", habilitado);

        // Determinar el estado final de JWT Local (nuevo o actual)
        boolean jwtLocalFinal = (jwtLocalEnabledNuevo != null) ? jwtLocalEnabledNuevo : esJwtLocalHabilitado();

        // Validación de seguridad: verificar que no se deshabiliten todos los métodos
        if (!habilitado && !jwtLocalFinal) {
            log.error("🚨 [SECURITY] Intento de deshabilitar Azure AD cuando JWT Local también estará deshabilitado");
            throw new IllegalStateException("No se puede deshabilitar Azure AD cuando JWT Local está deshabilitado. Al menos un método de autenticación debe estar activo.");
        }

        actualizarValor(AUTH_AZURE_AD_ENABLED, String.valueOf(habilitado));
    }

    /**
     * Habilita o deshabilita JWT Local con validación de seguridad
     */
    @CacheEvict(value = "configuracion", key = "T(com.example.apiprotegida.service.ConfiguracionService).AUTH_JWT_LOCAL_ENABLED")
    @Transactional
    public void establecerJwtLocalHabilitado(boolean habilitado, Boolean azureEnabledNuevo) {
        log.info("🔧 Cambiando estado de JWT Local a: {}", habilitado);

        // Determinar el estado final de Azure AD (nuevo o actual)
        boolean azureFinal = (azureEnabledNuevo != null) ? azureEnabledNuevo : esAzureAdHabilitado();

        // Validación de seguridad: verificar que no se deshabiliten todos los métodos
        if (!habilitado && !azureFinal) {
            log.error("🚨 [SECURITY] Intento de deshabilitar JWT Local cuando Azure AD también estará deshabilitado");
            throw new IllegalStateException("No se puede deshabilitar JWT Local cuando Azure AD está deshabilitado. Al menos un método de autenticación debe estar activo.");
        }

        actualizarValor(AUTH_JWT_LOCAL_ENABLED, String.valueOf(habilitado));
    }

    /**
     * Obtiene todas las configuraciones de autenticación
     */
    @Transactional(readOnly = true)
    public List<ConfiguracionSistema> obtenerConfiguracionesAutenticacion() {
        return configuracionRepository.findByCategoriaAndActivoTrue("AUTENTICACION");
    }

    /**
     * Obtiene todas las configuraciones activas
     */
    @Transactional(readOnly = true)
    public List<ConfiguracionSistema> obtenerTodasActivas() {
        return configuracionRepository.findByActivoTrue();
    }
}


