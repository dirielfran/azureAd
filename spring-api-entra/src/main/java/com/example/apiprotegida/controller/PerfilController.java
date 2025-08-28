package com.example.apiprotegida.controller;

import com.example.apiprotegida.model.Perfil;
import com.example.apiprotegida.service.PerfilService;
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
 * Controlador REST para gestión de perfiles
 */
@RestController
@RequestMapping("/perfiles")
@CrossOrigin(origins = {"http://localhost:4200", "https://localhost:4200", "http://localhost:4201", "https://localhost:4201"})
public class PerfilController {

    @Autowired
    private PerfilService perfilService;

    /**
     * Obtiene todos los perfiles activos
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<Perfil>> obtenerTodosLosPerfiles() {
        try {
            List<Perfil> perfiles = perfilService.obtenerTodosLosPerfiles();
            return ResponseEntity.ok(perfiles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene todos los perfiles con sus permisos
     */
    @GetMapping("/con-permisos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<Perfil>> obtenerPerfilesConPermisos() {
        try {
            List<Perfil> perfiles = perfilService.obtenerPerfilesConPermisos();
            return ResponseEntity.ok(perfiles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene un perfil por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Perfil> obtenerPerfilPorId(@PathVariable Long id) {
        try {
            Optional<Perfil> perfil = perfilService.obtenerPerfilPorId(id);
            return perfil.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene un perfil por nombre
     */
    @GetMapping("/nombre/{nombre}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Perfil> obtenerPerfilPorNombre(@PathVariable String nombre) {
        try {
            Optional<Perfil> perfil = perfilService.obtenerPerfilPorNombre(nombre);
            return perfil.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene un perfil por ID de grupo de Azure AD
     */
    @GetMapping("/azure-group/{azureGroupId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Perfil> obtenerPerfilPorAzureGroupId(@PathVariable String azureGroupId) {
        try {
            Optional<Perfil> perfil = perfilService.obtenerPerfilPorAzureGroupIdConPermisos(azureGroupId);
            return perfil.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crea un nuevo perfil
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crearPerfil(@Valid @RequestBody Perfil perfil) {
        try {
            Perfil nuevoPerfil = perfilService.crearPerfil(perfil);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPerfil);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponseMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createResponseMap("error", "Error interno del servidor"));
        }
    }

    /**
     * Actualiza un perfil existente
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarPerfil(@PathVariable Long id, @Valid @RequestBody Perfil perfil) {
        try {
            Perfil perfilActualizado = perfilService.actualizarPerfil(id, perfil);
            return ResponseEntity.ok(perfilActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponseMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createResponseMap("error", "Error interno del servidor"));
        }
    }

    /**
     * Desactiva un perfil (eliminación lógica)
     */
    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> desactivarPerfil(@PathVariable Long id) {
        try {
            perfilService.desactivarPerfil(id);
            return ResponseEntity.ok(createResponseMap("mensaje", "Perfil desactivado correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponseMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createResponseMap("error", "Error interno del servidor"));
        }
    }

    /**
     * Elimina un perfil permanentemente
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarPerfil(@PathVariable Long id) {
        try {
            perfilService.eliminarPerfil(id);
            return ResponseEntity.ok(createResponseMap("mensaje", "Perfil eliminado correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponseMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createResponseMap("error", "Error interno del servidor"));
        }
    }

    /**
     * Asigna un permiso a un perfil
     */
    @PostMapping("/{perfilId}/permisos/{permisoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> asignarPermiso(@PathVariable Long perfilId, @PathVariable Long permisoId) {
        try {
            Perfil perfil = perfilService.asignarPermiso(perfilId, permisoId);
            return ResponseEntity.ok(perfil);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponseMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createResponseMap("error", "Error interno del servidor"));
        }
    }

    /**
     * Remueve un permiso de un perfil
     */
    @DeleteMapping("/{perfilId}/permisos/{permisoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removerPermiso(@PathVariable Long perfilId, @PathVariable Long permisoId) {
        try {
            Perfil perfil = perfilService.removerPermiso(perfilId, permisoId);
            return ResponseEntity.ok(perfil);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponseMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createResponseMap("error", "Error interno del servidor"));
        }
    }

    /**
     * Asigna múltiples permisos a un perfil
     */
    @PostMapping("/{perfilId}/permisos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> asignarPermisos(@PathVariable Long perfilId, @RequestBody Set<Long> permisoIds) {
        try {
            Perfil perfil = perfilService.asignarPermisos(perfilId, permisoIds);
            return ResponseEntity.ok(perfil);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponseMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createResponseMap("error", "Error interno del servidor"));
        }
    }

    /**
     * Reemplaza todos los permisos de un perfil
     */
    @PutMapping("/{perfilId}/permisos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reemplazarPermisos(@PathVariable Long perfilId, @RequestBody Set<Long> permisoIds) {
        try {
            Perfil perfil = perfilService.reemplazarPermisos(perfilId, permisoIds);
            return ResponseEntity.ok(perfil);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponseMap("error", e.getMessage()));
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
