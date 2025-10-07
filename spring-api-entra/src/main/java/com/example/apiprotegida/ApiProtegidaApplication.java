package com.example.apiprotegida;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Aplicación Spring Boot para API protegida con Microsoft Entra ID
 * 
 * Esta aplicación proporciona una API REST segura que se integra con
 * Microsoft Entra ID (Azure AD) para autenticación y autorización.
 * 
 * Características principales:
 * - Autenticación JWT con Microsoft Entra ID
 * - CORS configurado para Angular
 * - Endpoints protegidos por scopes
 * - Base de datos H2 para desarrollo
 * - Actuator para monitoring
 * 
 * @author Elvis Areiza
 * @version 1.0
 */
@SpringBootApplication
@EntityScan("com.example.apiprotegida.model")
@EnableJpaRepositories("com.example.apiprotegida.repository")
@EnableCaching
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class ApiProtegidaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiProtegidaApplication.class, args);
        
        System.out.println("\n" +
            "╔══════════════════════════════════════════════════════════════╗\n" +
            "║                    🚀 API PROTEGIDA INICIADA 🚀              ║\n" +
            "║                                                              ║\n" +
            "║  📡 Servidor:        http://localhost:8080/api              ║\n" +
            "║  🔒 Autenticación:   Microsoft Entra ID                     ║\n" +
            "║  🌐 CORS:            http://localhost:4200                  ║\n" +
            "║  💾 Base de datos:   H2 (en memoria)                        ║\n" +
            "║  🔧 H2 Console:      http://localhost:8080/api/h2-console   ║\n" +
            "║  📊 Health Check:    http://localhost:8080/api/actuator/health ║\n" +
            "║                                                              ║\n" +
            "║  🔑 Client ID:       4a12fbd8-bf63-4c12-be4c-9678b207fbe7   ║\n" +
            "║  🏢 Tenant ID:       f128ae87-3797-42d7-8490-82c6b570f832   ║\n" +
            "║  📋 App ID URI:      api://4a12fbd8-bf63-4c12-be4c-9678b207fbe7 ║\n" +
            "║                                                              ║\n" +
            "║  ✅ Lista para recibir requests desde Angular!              ║\n" +
            "╚══════════════════════════════════════════════════════════════╝\n");
    }
}
