package com.example.apiprotegida.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Controlador para endpoints de datos protegidos
 * 
 * Proporciona varios endpoints de ejemplo para demostrar
 * la integración con Microsoft Entra ID desde Angular.
 */
@RestController
@RequestMapping("/data")
@CrossOrigin(origins = {"http://localhost:4200", "https://localhost:4200"})
@PreAuthorize("hasAuthority('SCOPE_access_as_user')")
public class DataController {

    /**
     * Obtiene datos básicos protegidos
     * @param authentication Información del usuario autenticado
     * @return Datos de ejemplo
     */
    @GetMapping
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
}
