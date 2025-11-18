package com.example.apiprotegida.service;

import com.example.apiprotegida.model.Perfil;
import com.example.apiprotegida.model.Usuario;
import com.example.apiprotegida.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de usuarios
 * Maneja tanto usuarios de Azure AD como usuarios locales
 */
@Service
@Transactional
@Slf4j
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilService perfilService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Obtiene un usuario por email y contraseña (para autenticación JWT)
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @return Usuario si las credenciales son válidas, null en caso contrario
     */
    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorEmailYPassword(String email, String password) {
        log.info("🔍 [UsuarioService] Buscando usuario por email: {}", email);
        
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            log.warn("❌ [UsuarioService] Usuario no encontrado con email: {}", email);
            return null;
        }

        Usuario usuario = usuarioOpt.get();
        log.info("✅ [UsuarioService] Usuario encontrado: {} - Activo: {}", usuario.getEmail(), usuario.getActivo());
        
        // Verificar que el usuario esté activo
        if (!usuario.getActivo()) {
            log.warn("❌ [UsuarioService] Usuario inactivo: {}", email);
            return null;
        }

        // Verificar contraseña
        if (usuario.getPassword() == null) {
            log.warn("❌ [UsuarioService] Usuario sin contraseña: {}", email);
            return null;
        }
        
        boolean passwordMatches = passwordEncoder.matches(password, usuario.getPassword());
        log.info("🔐 [UsuarioService] Verificación de contraseña para {}: {}", email, passwordMatches);
        
        if (!passwordMatches) {
            log.warn("❌ [UsuarioService] Contraseña incorrecta para usuario: {}", email);
            return null;
        }

        log.info("✅ [UsuarioService] Usuario autenticado exitosamente: {}", email);
        return usuario;
    }

    /**
     * Obtiene un usuario por email (para Azure AD)
     * @param email Email del usuario
     * @return Usuario si existe, null en caso contrario
     */
    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorEmail(String email) {
        log.debug("Buscando usuario por email: {}", email);
        
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            log.debug("Usuario no encontrado con email: {}", email);
            return null;
        }

        Usuario usuario = usuarioOpt.get();
        
        // Verificar que el usuario esté activo
        if (!usuario.getActivo()) {
            log.debug("Usuario inactivo: {}", email);
            return null;
        }

        log.debug("Usuario encontrado: {}", email);
        return usuario;
    }

    /**
     * Obtiene un usuario por Azure Object ID
     * @param azureObjectId Object ID de Azure AD
     * @return Usuario si existe, null en caso contrario
     */
    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorAzureObjectId(String azureObjectId) {
        log.debug("Buscando usuario por Azure Object ID: {}", azureObjectId);
        
        Optional<Usuario> usuarioOpt = usuarioRepository.findByAzureObjectId(azureObjectId);
        if (usuarioOpt.isEmpty()) {
            log.debug("Usuario no encontrado con Azure Object ID: {}", azureObjectId);
            return null;
        }

        Usuario usuario = usuarioOpt.get();
        
        // Verificar que el usuario esté activo
        if (!usuario.getActivo()) {
            log.debug("Usuario inactivo: {}", azureObjectId);
            return null;
        }

        log.debug("Usuario encontrado: {}", azureObjectId);
        return usuario;
    }

    /**
     * Crea un nuevo usuario local
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @param nombre Nombre del usuario
     * @return Usuario creado
     */
    @Transactional
    public Usuario crearUsuarioLocal(String email, String password, String nombre) {
        log.debug("Creando usuario local: {}", email);
        
        // Verificar que el email no exista
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado: " + email);
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setNombre(nombre);
        usuario.setActivo(true);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        log.debug("Usuario local creado exitosamente: {}", email);
        
        return usuarioGuardado;
    }

    /**
     * Obtiene todos los usuarios activos
     * @return Lista de usuarios activos
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerTodosLosUsuarios() {
        log.debug("Obteniendo todos los usuarios activos");
        
        return usuarioRepository.findByActivo(true);
    }

    /**
     * Obtiene usuarios por departamento
     * @param departamento Departamento a buscar
     * @return Lista de usuarios del departamento
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerUsuariosPorDepartamento(String departamento) {
        log.debug("Obteniendo usuarios por departamento: {}", departamento);
        
        return usuarioRepository.findByDepartamento(departamento);
    }

    /**
     * Actualiza la contraseña de un usuario
     * @param usuario Usuario al que se le actualizará la contraseña
     * @param nuevaPassword Nueva contraseña en texto plano
     */
    @Transactional
    public void actualizarPassword(Usuario usuario, String nuevaPassword) {
        log.info("🔄 [UsuarioService] Actualizando contraseña para usuario: {}", usuario.getEmail());
        
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        
        if (nuevaPassword == null || nuevaPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        
        // Validar longitud mínima de contraseña
        if (nuevaPassword.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
        
        // Codificar y actualizar contraseña
        String passwordEncriptada = passwordEncoder.encode(nuevaPassword);
        usuario.setPassword(passwordEncriptada);
        usuarioRepository.save(usuario);
        
        log.info("✅ [UsuarioService] Contraseña actualizada exitosamente para: {}", usuario.getEmail());
    }
}
