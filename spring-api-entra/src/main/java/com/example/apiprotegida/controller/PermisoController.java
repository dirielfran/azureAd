package com.example.apiprotegida.controller;

import com.example.apiprotegida.model.Permiso;
import com.example.apiprotegida.service.PermisoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Controlador REST para gestión de permisos
 */
@RestController
@RequestMapping("/permisos")
@CrossOrigin(origins = {"http://localhost:4200", "https://localhost:4200", "http://localhost:4201", "https://localhost:4201"})
public class PermisoController {

    @Autowired
    private PermisoService permisoService;

    /**
     * Obtiene todos los permisos activos
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<Permiso>> obtenerTodosLosPermisos() {
        try {
            List<Permiso> permisos = permisoService.obtenerTodosLosPermisos();
            return ResponseEntity.ok(permisos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene un permiso por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Permiso> obtenerPermisoPorId(@PathVariable Long id) {
        try {
            Optional<Permiso> permiso = permisoService.obtenerPermisoPorId(id);
            return permiso.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene un permiso por código
     */
    @GetMapping("/codigo/{codigo}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Permiso> obtenerPermisoPorCodigo(@PathVariable String codigo) {
        try {
            Optional<Permiso> permiso = permisoService.obtenerPermisoPorCodigo(codigo);
            return permiso.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene permisos por módulo
     */
    @GetMapping("/modulo/{modulo}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<Permiso>> obtenerPermisosPorModulo(@PathVariable String modulo) {
        try {
            List<Permiso> permisos = permisoService.obtenerPermisosPorModulo(modulo);
            return ResponseEntity.ok(permisos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene permisos por acción
     */
    @GetMapping("/accion/{accion}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<Permiso>> obtenerPermisosPorAccion(@PathVariable String accion) {
        try {
            List<Permiso> permisos = permisoService.obtenerPermisosPorAccion(accion);
            return ResponseEntity.ok(permisos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene permisos por módulo y acción
     */
    @GetMapping("/modulo/{modulo}/accion/{accion}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<Permiso>> obtenerPermisosPorModuloYAccion(
            @PathVariable String modulo, 
            @PathVariable String accion) {
        try {
            List<Permiso> permisos = permisoService.obtenerPermisosPorModuloYAccion(modulo, accion);
            return ResponseEntity.ok(permisos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene todos los módulos únicos
     */
    @GetMapping("/modulos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<String>> obtenerModulos() {
        try {
            List<String> modulos = permisoService.obtenerModulos();
            return ResponseEntity.ok(modulos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene todas las acciones únicas
     */
    @GetMapping("/acciones")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<String>> obtenerAcciones() {
        try {
            List<String> acciones = permisoService.obtenerAcciones();
            return ResponseEntity.ok(acciones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene permisos asociados a un perfil
     */
    @GetMapping("/perfil/{perfilId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<Permiso>> obtenerPermisosPorPerfilId(@PathVariable Long perfilId) {
        try {
            List<Permiso> permisos = permisoService.obtenerPermisosPorPerfilId(perfilId);
            return ResponseEntity.ok(permisos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crea un nuevo permiso
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crearPermiso(@Valid @RequestBody Permiso permiso) {
        try {
            Permiso nuevoPermiso = permisoService.crearPermiso(permiso);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPermiso);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponseMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createResponseMap("error", "Error interno del servidor"));
        }
    }

    /**
     * Actualiza un permiso existente
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarPermiso(@PathVariable Long id, @Valid @RequestBody Permiso permiso) {
        try {
            Permiso permisoActualizado = permisoService.actualizarPermiso(id, permiso);
            return ResponseEntity.ok(permisoActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponseMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createResponseMap("error", "Error interno del servidor"));
        }
    }

    /**
     * Desactiva un permiso (eliminación lógica)
     */
    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> desactivarPermiso(@PathVariable Long id) {
        try {
            permisoService.desactivarPermiso(id);
            return ResponseEntity.ok(createResponseMap("mensaje", "Permiso desactivado correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponseMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createResponseMap("error", "Error interno del servidor"));
        }
    }

    /**
     * Elimina un permiso permanentemente
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarPermiso(@PathVariable Long id) {
        try {
            permisoService.eliminarPermiso(id);
            return ResponseEntity.ok(createResponseMap("mensaje", "Permiso eliminado correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponseMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createResponseMap("error", "Error interno del servidor"));
        }
    }

    /**
     * Obtiene estadísticas de permisos por módulo
     */
    @GetMapping("/estadisticas/modulo")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<Object[]>> obtenerEstadisticasPorModulo() {
        try {
            List<Object[]> estadisticas = permisoService.obtenerEstadisticasPorModulo();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Verifica si existe un permiso con el código dado
     */
    @GetMapping("/existe/{codigo}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> existePermiso(@PathVariable String codigo) {
        try {
            boolean existe = permisoService.existePermiso(codigo);
            return ResponseEntity.ok(createResponseMap("existe", String.valueOf(existe)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createResponseMap("error", "Error interno del servidor"));
        }
    }

    // Método auxiliar para crear mapas de respuesta
    private static java.util.Map<String, String> createResponseMap(String key, String value) {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        map.put(key, value);
        return map;
    }
}
