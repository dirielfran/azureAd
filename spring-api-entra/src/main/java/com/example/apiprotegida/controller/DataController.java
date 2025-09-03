package com.example.apiprotegida.controller;

import com.example.apiprotegida.security.RoleAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador para endpoints de datos protegidos
 * 
 * Proporciona varios endpoints de ejemplo para demostrar
 * la integración con Microsoft Entra ID desde Angular.
 */
@RestController
@RequestMapping("/data")
@CrossOrigin(origins = {"http://localhost:4200", "https://localhost:4200"})
public class DataController {

    /**
     * Obtiene datos básicos protegidos
     * @param authentication Información del usuario autenticado
     * @return Datos de ejemplo
     */
    @GetMapping
    @RoleAnnotations.ValidScope
    public ResponseEntity<Map<String, Object>> getData(Authentication authentication) {
        Map<String, Object> data = new HashMap<>();
        
        data.put("message", "¡Datos obtenidos exitosamente desde la API protegida!");
        data.put("timestamp", LocalDateTime.now());
        data.put("server", "Spring Boot API");
        data.put("version", "1.0.0");
        
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            data.put("user", jwt.getClaimAsString("name"));
            data.put("email", jwt.getClaimAsString("email"));
        }
        
        // Datos de ejemplo
        data.put("datos_ejemplo", Arrays.asList(
            Map.of("id", 1, "nombre", "Producto A", "precio", 29.99, "categoria", "Electrónicos"),
            Map.of("id", 2, "nombre", "Producto B", "precio", 19.99, "categoria", "Hogar"),
            Map.of("id", 3, "nombre", "Producto C", "precio", 39.99, "categoria", "Deportes")
        ));
        
        return ResponseEntity.ok(data);
    }

    /**
     * Obtiene datos de dashboard
     * @return Datos para un dashboard
     */
    @GetMapping("/dashboard")
    @RoleAnnotations.AdminManagerOrUser
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Métricas de ejemplo
        dashboard.put("metricas", Map.of(
            "ventas_mes", 15420.50,
            "usuarios_activos", 1247,
            "pedidos_pendientes", 23,
            "productos_stock", 156
        ));
        
        // Gráfico de ventas (datos de ejemplo)
        List<Map<String, Object>> ventasPorDia = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            ventasPorDia.add(Map.of(
                "dia", "Día " + i,
                "ventas", 1000 + (Math.random() * 2000)
            ));
        }
        dashboard.put("ventas_por_dia", ventasPorDia);
        
        // Top productos
        dashboard.put("top_productos", Arrays.asList(
            Map.of("nombre", "Laptop Gaming", "ventas", 45),
            Map.of("nombre", "Mouse Inalámbrico", "ventas", 38),
            Map.of("nombre", "Teclado Mecánico", "ventas", 32),
            Map.of("nombre", "Monitor 4K", "ventas", 28),
            Map.of("nombre", "Webcam HD", "ventas", 21)
        ));
        
        dashboard.put("ultima_actualizacion", LocalDateTime.now());
        
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Simula una operación que requiere tiempo de procesamiento
     * @return Resultado de la operación
     */
    @GetMapping("/proceso-lento")
    public ResponseEntity<Map<String, Object>> procesoLento() throws InterruptedException {
        // Simular procesamiento
        Thread.sleep(2000); // 2 segundos
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("message", "Proceso completado exitosamente");
        resultado.put("duracion", "2 segundos");
        resultado.put("timestamp", LocalDateTime.now());
        resultado.put("resultado", Map.of(
            "procesados", 1000,
            "exitosos", 987,
            "errores", 13,
            "porcentaje_exito", 98.7
        ));
        
        return ResponseEntity.ok(resultado);
    }

    /**
     * Endpoint para crear datos
     * @param datos Datos a procesar
     * @return Confirmación de creación
     */
    @PostMapping
    @RoleAnnotations.AdminOrManager
    public ResponseEntity<Map<String, Object>> createData(@RequestBody Map<String, Object> datos) {
        Map<String, Object> response = new HashMap<>();
        
        response.put("message", "Datos creados exitosamente");
        response.put("id", UUID.randomUUID().toString());
        response.put("datos_recibidos", datos);
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "created");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene configuración de la aplicación
     * @return Configuración disponible para el usuario
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig(Authentication authentication) {
        Map<String, Object> config = new HashMap<>();
        
        config.put("app_name", "API Protegida Demo");
        config.put("version", "1.0.0");
        config.put("features", Arrays.asList(
            "Autenticación Microsoft Entra ID",
            "CORS configurado para Angular",
            "JWT Token validation",
            "Base de datos H2",
            "Swagger documentation"
        ));
        
        // Configuración específica del usuario
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            Map<String, Object> userConfig = new HashMap<>();
            userConfig.put("theme", "default");
            userConfig.put("language", "es");
            userConfig.put("notifications", true);
            userConfig.put("user_id", jwt.getClaimAsString("oid"));
            
            config.put("user_preferences", userConfig);
        }
        
        return ResponseEntity.ok(config);
    }

    /**
     * Endpoint para reportes
     * @param tipo Tipo de reporte a generar
     * @return Datos del reporte
     */
    @GetMapping("/reportes/{tipo}")
    public ResponseEntity<Map<String, Object>> getReporte(@PathVariable String tipo) {
        Map<String, Object> reporte = new HashMap<>();
        
        switch (tipo.toLowerCase()) {
            case "ventas":
                reporte.put("titulo", "Reporte de Ventas");
                reporte.put("periodo", "Último mes");
                reporte.put("total_ventas", 45280.75);
                reporte.put("transacciones", 156);
                reporte.put("promedio_por_venta", 290.26);
                break;
                
            case "usuarios":
                reporte.put("titulo", "Reporte de Usuarios");
                reporte.put("total_usuarios", 1247);
                reporte.put("usuarios_activos", 1156);
                reporte.put("nuevos_este_mes", 89);
                reporte.put("tasa_retencion", 92.7);
                break;
                
            case "productos":
                reporte.put("titulo", "Reporte de Productos");
                reporte.put("total_productos", 234);
                reporte.put("en_stock", 198);
                reporte.put("agotados", 36);
                reporte.put("mas_vendido", "Laptop Gaming");
                break;
                
            default:
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Tipo de reporte no válido",
                    "tipos_disponibles", Arrays.asList("ventas", "usuarios", "productos")
                ));
        }
        
        reporte.put("generado_en", LocalDateTime.now());
        reporte.put("tipo", tipo);
        
        return ResponseEntity.ok(reporte);
    }

    /**
     * Endpoint para diagnosticar problemas de autorización
     * @return Información detallada del token y permisos
     */
    @GetMapping("/debug-auth")
    @RoleAnnotations.ValidScope
    public ResponseEntity<Map<String, Object>> debugAuth(Authentication authentication) {
        Map<String, Object> debug = new HashMap<>();
        
        debug.put("authenticated", authentication.isAuthenticated());
        debug.put("authorities", authentication.getAuthorities());
        debug.put("principal_type", authentication.getPrincipal().getClass().getSimpleName());
        
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            debug.put("jwt_claims", jwt.getClaims());
            debug.put("groups_claim", jwt.getClaimAsStringList("groups"));
            debug.put("roles_claim", jwt.getClaimAsStringList("roles"));
            debug.put("scopes", jwt.getClaimAsStringList("scp"));
            debug.put("user_email", jwt.getClaimAsString("email"));
            debug.put("user_name", jwt.getClaimAsString("name"));
            
            // Análisis específico de roles
            List<String> roleAuthorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .collect(Collectors.toList());
            debug.put("role_authorities", roleAuthorities);
            
            // Verificar si tiene los roles necesarios para AdminManagerOrUser
            boolean hasAdminRole = roleAuthorities.contains("ROLE_ADMIN");
            boolean hasManagerRole = roleAuthorities.contains("ROLE_MANAGER");
            boolean hasUserRole = roleAuthorities.contains("ROLE_USER");
            
            debug.put("has_admin_role", hasAdminRole);
            debug.put("has_manager_role", hasManagerRole);
            debug.put("has_user_role", hasUserRole);
            debug.put("can_access_dashboard", hasAdminRole || hasManagerRole || hasUserRole);
        }
        
        return ResponseEntity.ok(debug);
    }

    /**
     * Endpoint para testing de conectividad
     * @return Status de la API
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("uptime", System.currentTimeMillis());
        health.put("services", Map.of(
            "database", "UP",
            "authentication", "UP",
            "external_apis", "UP"
        ));
        
        return ResponseEntity.ok(health);
    }

    // ===============================================
    // ENDPOINTS CON AUTORIZACIÓN ESPECÍFICA POR ROL
    // ===============================================

    /**
     * Endpoint administrativo - solo para ADMIN
     * @return Datos administrativos sensibles
     */
    @GetMapping("/admin/sensitive-data")
    @RoleAnnotations.AdminOnly
    public ResponseEntity<Map<String, Object>> getAdminSensitiveData() {
        Map<String, Object> adminData = new HashMap<>();
        
        adminData.put("message", "🔐 Datos administrativos sensibles");
        adminData.put("server_config", Map.of(
            "database_connections", 25,
            "memory_usage", "2.1GB",
            "active_sessions", 156,
            "security_level", "HIGH"
        ));
        adminData.put("system_logs", Arrays.asList(
            "2024-01-15 10:30:00 - Sistema iniciado correctamente",
            "2024-01-15 10:31:15 - Conexión a base de datos establecida",
            "2024-01-15 10:32:00 - Configuración de seguridad cargada"
        ));
        adminData.put("access_level", "ADMIN_ONLY");
        adminData.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(adminData);
    }

    /**
     * Endpoint de gestión - para ADMIN y MANAGER
     * @return Datos de gestión
     */
    @GetMapping("/manager/reports")
    @RoleAnnotations.AdminOrManager
    public ResponseEntity<Map<String, Object>> getManagerReports() {
        Map<String, Object> managerData = new HashMap<>();
        
        managerData.put("message", "📊 Reportes de gestión");
        managerData.put("sales_summary", Map.of(
            "total_sales", 125600.75,
            "monthly_growth", 12.5,
            "top_regions", Arrays.asList("Norte", "Centro", "Sur"),
            "pending_approvals", 8
        ));
        managerData.put("team_metrics", Map.of(
            "active_employees", 45,
            "productivity_score", 87.3,
            "satisfaction_rate", 94.2
        ));
        managerData.put("access_level", "MANAGER_OR_ADMIN");
        managerData.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(managerData);
    }

    /**
     * Endpoint de usuario - para ADMIN, MANAGER, USER
     * @return Datos de usuario estándar
     */
    @GetMapping("/user/profile-data")
    @RoleAnnotations.AdminManagerOrUser
    public ResponseEntity<Map<String, Object>> getUserProfileData(Authentication authentication) {
        Map<String, Object> userData = new HashMap<>();
        
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            userData.put("message", "👤 Datos de perfil de usuario");
            userData.put("user_info", Map.of(
                "name", jwt.getClaimAsString("name"),
                "email", jwt.getClaimAsString("email"),
                "department", "Desarrollo", // Ejemplo
                "role", authentication.getAuthorities().toString()
            ));
            userData.put("preferences", Map.of(
                "theme", "dark",
                "language", "es",
                "notifications_enabled", true,
                "dashboard_layout", "grid"
            ));
            userData.put("recent_activity", Arrays.asList(
                "Accedió al dashboard - " + LocalDateTime.now().minusHours(2),
                "Actualizó perfil - " + LocalDateTime.now().minusDays(1),
                "Descargó reporte - " + LocalDateTime.now().minusDays(3)
            ));
        }
        
        userData.put("access_level", "USER_OR_HIGHER");
        userData.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(userData);
    }

    /**
     * Endpoint con autorización personalizada usando SpEL
     * Solo permite acceso si el usuario tiene rol ADMIN O si es MANAGER y el email contiene "manager"
     */
    @GetMapping("/custom/conditional-access")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and authentication.name.contains('manager'))")
    public ResponseEntity<Map<String, Object>> getConditionalAccess(Authentication authentication) {
        Map<String, Object> data = new HashMap<>();
        
        data.put("message", "🎯 Acceso condicional concedido");
        data.put("condition", "ADMIN OR (MANAGER + email contains 'manager')");
        data.put("user", authentication.getName());
        data.put("authorities", authentication.getAuthorities());
        data.put("access_granted_at", LocalDateTime.now());
        
        return ResponseEntity.ok(data);
    }
}
