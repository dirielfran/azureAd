package com.example.apiprotegida.controller;

import com.example.apiprotegida.model.Permiso;
import com.example.apiprotegida.service.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controlador REST para autorización y gestión de permisos de usuario
 */
@RestController
@RequestMapping("/autorizacion")
@CrossOrigin(origins = {"http://localhost:4200", "https://localhost:4200", "http://localhost:4201", "https://localhost:4201"})
public class AuthorizationController {

    @Autowired
    private AuthorizationService authorizationService;

    /**
     * Obtiene la información completa del usuario autenticado incluyendo permisos
     */
    @GetMapping("/informacion-usuario")
    public ResponseEntity<Map<String, Object>> obtenerInformacionUsuario(Authentication authentication) {
        try {
            Map<String, Object> informacionUsuario = authorizationService.obtenerInformacionCompleteUsuario(authentication);
            return ResponseEntity.ok(informacionUsuario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createMap("error", "Error al obtener información del usuario"));
        }
    }

    /**
     * Obtiene solo los permisos del usuario autenticado
     */
    @GetMapping("/permisos")
    public ResponseEntity<List<Permiso>> obtenerPermisosUsuario(Authentication authentication) {
        try {
            List<Permiso> permisos = authorizationService.obtenerPermisosUsuario(authentication);
            return ResponseEntity.ok(permisos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene solo los códigos de permisos del usuario autenticado
     */
    @GetMapping("/codigos-permisos")
    public ResponseEntity<Set<String>> obtenerCodigosPermisos(Authentication authentication) {
        try {
            Set<String> codigosPermisos = authorizationService.obtenerCodigosPermisosUsuario(authentication);
            return ResponseEntity.ok(codigosPermisos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Verifica si el usuario tiene un permiso específico
     */
    @GetMapping("/tiene-permiso/{codigoPermiso}")
    public ResponseEntity<Map<String, Boolean>> tienePermiso(
            @PathVariable String codigoPermiso, 
            Authentication authentication) {
        try {
            boolean tienePermiso = authorizationService.tienePermiso(authentication, codigoPermiso);
            return ResponseEntity.ok(createBooleanMap("tienePermiso", tienePermiso));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createBooleanMap("tienePermiso", false));
        }
    }

    /**
     * Verifica múltiples permisos a la vez
     */
    @PostMapping("/verificar-permisos")
    public ResponseEntity<Map<String, Boolean>> verificarPermisos(
            @RequestBody List<String> codigosPermisos, 
            Authentication authentication) {
        try {
            Map<String, Boolean> resultados = new java.util.HashMap<>();
            Set<String> permisosUsuario = authorizationService.obtenerCodigosPermisosUsuario(authentication);
            
            for (String codigo : codigosPermisos) {
                resultados.put(codigo, permisosUsuario.contains(codigo));
            }
            
            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Verifica si el usuario tiene permisos para un módulo específico
     */
    @GetMapping("/tiene-permiso-modulo/{modulo}")
    public ResponseEntity<Map<String, Boolean>> tienePermisoEnModulo(
            @PathVariable String modulo, 
            Authentication authentication) {
        try {
            boolean tienePermiso = authorizationService.tienePermisoEnModulo(authentication, modulo);
            return ResponseEntity.ok(createBooleanMap("tienePermisoModulo", tienePermiso));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createBooleanMap("tienePermisoModulo", false));
        }
    }

    /**
     * Verifica si el usuario tiene permisos para una acción específica
     */
    @GetMapping("/tiene-permiso-accion/{accion}")
    public ResponseEntity<Map<String, Boolean>> tienePermisoParaAccion(
            @PathVariable String accion, 
            Authentication authentication) {
        try {
            boolean tienePermiso = authorizationService.tienePermisoParaAccion(authentication, accion);
            return ResponseEntity.ok(createBooleanMap("tienePermisoAccion", tienePermiso));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createBooleanMap("tienePermisoAccion", false));
        }
    }

    /**
     * Verifica si el usuario tiene permisos para una acción en un módulo específico
     */
    @GetMapping("/tiene-permiso-modulo-accion/{modulo}/{accion}")
    public ResponseEntity<Map<String, Boolean>> tienePermisoEnModuloYAccion(
            @PathVariable String modulo, 
            @PathVariable String accion, 
            Authentication authentication) {
        try {
            boolean tienePermiso = authorizationService.tienePermisoEnModuloYAccion(authentication, modulo, accion);
            return ResponseEntity.ok(createBooleanMap("tienePermisoModuloAccion", tienePermiso));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createBooleanMap("tienePermisoModuloAccion", false));
        }
    }

    /**
     * Endpoint para validar múltiples tipos de permisos
     */
    @PostMapping("/validar-acceso")
    public ResponseEntity<Map<String, Object>> validarAcceso(
            @RequestBody Map<String, Object> solicitudValidacion, 
            Authentication authentication) {
        try {
            Map<String, Object> resultado = new java.util.HashMap<>();
            
            // Validar permisos específicos si se proporcionan
            if (solicitudValidacion.containsKey("permisos")) {
                @SuppressWarnings("unchecked")
                List<String> permisos = (List<String>) solicitudValidacion.get("permisos");
                Map<String, Boolean> resultadosPermisos = new java.util.HashMap<>();
                Set<String> permisosUsuario = authorizationService.obtenerCodigosPermisosUsuario(authentication);
                
                for (String permiso : permisos) {
                    resultadosPermisos.put(permiso, permisosUsuario.contains(permiso));
                }
                resultado.put("permisos", resultadosPermisos);
            }
            
            // Validar módulos si se proporcionan
            if (solicitudValidacion.containsKey("modulos")) {
                @SuppressWarnings("unchecked")
                List<String> modulos = (List<String>) solicitudValidacion.get("modulos");
                Map<String, Boolean> resultadosModulos = new java.util.HashMap<>();
                
                for (String modulo : modulos) {
                    resultadosModulos.put(modulo, authorizationService.tienePermisoEnModulo(authentication, modulo));
                }
                resultado.put("modulos", resultadosModulos);
            }
            
            // Validar acciones si se proporcionan
            if (solicitudValidacion.containsKey("acciones")) {
                @SuppressWarnings("unchecked")
                List<String> acciones = (List<String>) solicitudValidacion.get("acciones");
                Map<String, Boolean> resultadosAcciones = new java.util.HashMap<>();
                
                for (String accion : acciones) {
                    resultadosAcciones.put(accion, authorizationService.tienePermisoParaAccion(authentication, accion));
                }
                resultado.put("acciones", resultadosAcciones);
            }
            
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createMap("error", "Error al validar acceso"));
        }
    }

    // Método auxiliar para crear mapas
    private static java.util.Map<String, Object> createMap(String key, Object value) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put(key, value);
        return map;
    }
    
    private static java.util.Map<String, Boolean> createBooleanMap(String key, Boolean value) {
        java.util.Map<String, Boolean> map = new java.util.HashMap<>();
        map.put(key, value);
        return map;
    }
}
