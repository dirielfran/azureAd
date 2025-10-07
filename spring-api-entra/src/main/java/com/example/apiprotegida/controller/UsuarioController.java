package com.example.apiprotegida.controller;

import com.example.apiprotegida.model.Usuario;
import com.example.apiprotegida.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para la gestión de usuarios
 * 
 * Todos los endpoints requieren autenticación con Microsoft Entra ID
 * y el scope 'access_as_user'.
 */
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = {"http://localhost:4200", "https://localhost:4200"})
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtiene todos los usuarios
     * Acepta tanto tokens de Azure AD como JWT locales
     * @return Lista de todos los usuarios
     */
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_access_as_user') or hasAuthority('USUARIOS_LEER')")
    public ResponseEntity<List<Usuario>> getAllUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Obtiene un usuario por ID
     * @param id ID del usuario
     * @return Usuario encontrado o 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        return usuario.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo usuario
     * @param usuario Datos del usuario a crear
     * @param authentication Información del usuario autenticado
     * @return Usuario creado
     */
    @PostMapping
    public ResponseEntity<Usuario> createUsuario(@Valid @RequestBody Usuario usuario, 
                                               Authentication authentication) {
        // Si el usuario autenticado no tiene Azure Object ID, agregarlo
        if (usuario.getAzureObjectId() == null && authentication.getPrincipal() instanceof Jwt jwt) {
            usuario.setAzureObjectId(jwt.getClaimAsString("oid"));
        }
        
        // Verificar que no exista un usuario con el mismo email
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        Usuario nuevoUsuario = usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
    }

    /**
     * Actualiza un usuario existente
     * @param id ID del usuario a actualizar
     * @param usuarioActualizado Datos actualizados del usuario
     * @return Usuario actualizado o 404 si no existe
     */
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> updateUsuario(@PathVariable Long id, 
                                               @Valid @RequestBody Usuario usuarioActualizado) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setNombre(usuarioActualizado.getNombre());
                    usuario.setEmail(usuarioActualizado.getEmail());
                    usuario.setDepartamento(usuarioActualizado.getDepartamento());
                    usuario.setCargo(usuarioActualizado.getCargo());
                    usuario.setActivo(usuarioActualizado.getActivo());
                    
                    return ResponseEntity.ok(usuarioRepository.save(usuario));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Elimina un usuario
     * @param id ID del usuario a eliminar
     * @return 204 si se eliminó correctamente, 404 si no existe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuarioRepository.delete(usuario);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca usuarios por departamento
     * @param departamento Nombre del departamento
     * @return Lista de usuarios del departamento
     */
    @GetMapping("/departamento/{departamento}")
    public ResponseEntity<List<Usuario>> getUsuariosByDepartamento(@PathVariable String departamento) {
        List<Usuario> usuarios = usuarioRepository.findByDepartamento(departamento);
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Busca usuarios por nombre (búsqueda parcial)
     * @param nombre Nombre o parte del nombre a buscar
     * @return Lista de usuarios que coinciden
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Usuario>> buscarUsuarios(@RequestParam String nombre) {
        List<Usuario> usuarios = usuarioRepository.findByNombreContainingIgnoreCase(nombre);
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Obtiene estadísticas de usuarios
     * @return Estadísticas generales
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total_usuarios", usuarioRepository.count());
        stats.put("usuarios_activos", usuarioRepository.findByActivo(true).size());
        stats.put("usuarios_inactivos", usuarioRepository.findByActivo(false).size());
        stats.put("departamentos", usuarioRepository.findAllDepartamentos());
        
        // Estadísticas por departamento
        Map<String, Long> porDepartamento = new HashMap<>();
        for (String dept : usuarioRepository.findAllDepartamentos()) {
            porDepartamento.put(dept, usuarioRepository.countActiveUsersByDepartamento(dept));
        }
        stats.put("usuarios_por_departamento", porDepartamento);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Obtiene el perfil del usuario actual basado en su token
     * @param authentication Información del usuario autenticado
     * @return Perfil del usuario o información del token si no existe en BD
     */
    @GetMapping("/mi-perfil")
    public ResponseEntity<Map<String, Object>> getMiPerfil(Authentication authentication) {
        Map<String, Object> perfil = new HashMap<>();
        
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String azureObjectId = jwt.getClaimAsString("oid");
            
            // Buscar usuario en la base de datos
            Optional<Usuario> usuario = usuarioRepository.findByAzureObjectId(azureObjectId);
            
            if (usuario.isPresent()) {
                perfil.put("usuario_bd", usuario.get());
                perfil.put("existe_en_bd", true);
            } else {
                perfil.put("existe_en_bd", false);
            }
            
            // Información del token
            perfil.put("azure_info", Map.of(
                "object_id", azureObjectId,
                "name", jwt.getClaimAsString("name"),
                "email", jwt.getClaimAsString("email"),
                "preferred_username", jwt.getClaimAsString("preferred_username"),
                "given_name", jwt.getClaimAsString("given_name"),
                "family_name", jwt.getClaimAsString("family_name")
            ));
        }
        
        return ResponseEntity.ok(perfil);
    }

    /**
     * Crea o actualiza el perfil del usuario actual
     * @param usuario Datos del usuario
     * @param authentication Información del usuario autenticado
     * @return Usuario creado o actualizado
     */
    @PostMapping("/mi-perfil")
    public ResponseEntity<Usuario> crearMiPerfil(@Valid @RequestBody Usuario usuario, 
                                               Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String azureObjectId = jwt.getClaimAsString("oid");
            String email = jwt.getClaimAsString("email");
            String nombre = jwt.getClaimAsString("name");
            
            // Buscar si ya existe
            Optional<Usuario> usuarioExistente = usuarioRepository.findByAzureObjectId(azureObjectId);
            
            if (usuarioExistente.isPresent()) {
                // Actualizar usuario existente
                Usuario usuarioActual = usuarioExistente.get();
                usuarioActual.setNombre(usuario.getNombre() != null ? usuario.getNombre() : nombre);
                usuarioActual.setEmail(usuario.getEmail() != null ? usuario.getEmail() : email);
                usuarioActual.setDepartamento(usuario.getDepartamento());
                usuarioActual.setCargo(usuario.getCargo());
                usuarioActual.setActivo(usuario.getActivo());
                
                return ResponseEntity.ok(usuarioRepository.save(usuarioActual));
            } else {
                // Crear nuevo usuario
                usuario.setAzureObjectId(azureObjectId);
                if (usuario.getNombre() == null) usuario.setNombre(nombre);
                if (usuario.getEmail() == null) usuario.setEmail(email);
                
                return ResponseEntity.status(HttpStatus.CREATED).body(usuarioRepository.save(usuario));
            }
        }
        
        return ResponseEntity.badRequest().build();
    }
}
