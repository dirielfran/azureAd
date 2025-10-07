package com.example.apiprotegida.controller;

import com.example.apiprotegida.model.Usuario;
import com.example.apiprotegida.exceptions.UnauthorizedException;
import com.example.apiprotegida.security.JWTTokenProvider;
import com.example.apiprotegida.service.UsuarioService;
import com.example.apiprotegida.shared.dto.UserDTO;
import com.example.apiprotegida.shared.dto.responses.ResponseJWTDTO;
import com.example.apiprotegida.shared.dto.responses.ResponseUserDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Controlador BFF (Backend for Frontend) para autenticación JWT
 * Maneja el login y autenticación de usuarios locales
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class BFFUserController {
    
    private final JWTTokenProvider jwtTokenProvider;
    private final UsuarioService usuarioService;

    @Autowired
    public BFFUserController(JWTTokenProvider jwtTokenProvider, UsuarioService usuarioService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.usuarioService = usuarioService;
    }

    /**
     * Obtiene información de un usuario por ID (método mock)
     * @param id ID del usuario
     * @return Información del usuario
     */
    @GetMapping("/user/{id}")
    ResponseEntity<ResponseUserDTO> obtenerUserMock(@PathVariable Long id) {
        log.debug("Obteniendo usuario mock con ID: {}", id);
        
        ResponseUserDTO response = new ResponseUserDTO();
        if (obtenerUserId(id) == null) {
            response.setError(true);
            response.addErrorMessage("ERROR_TOKEN", "Sesión expirada");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        response.setUser(obtenerUserId(id));
        return ResponseEntity.ok().body(response);
    }

    /**
     * Login mock para usuarios locales (método de compatibilidad)
     * @param userDTO Datos del usuario
     * @return Información del usuario autenticado
     */
    @PostMapping
    ResponseEntity<ResponseUserDTO> loginUserMock(@RequestBody @Valid UserDTO userDTO) {
        log.debug("Login mock para usuario: {}", userDTO.getUser());
        
        ResponseUserDTO response = new ResponseUserDTO();

        if (validarLoginUser(userDTO) == null) {
            response.setError(true);
            response.addErrorMessage("ERROR", "Usuario o contraseña incorrectos!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        response.setUser(validarLoginUser(userDTO));
        response.getUser().setPassword("****");
        return ResponseEntity.ok().body(response);
    }

    /**
     * Genera hash BCrypt para una contraseña (endpoint temporal para debugging)
     * @param password Contraseña a hashear
     * @return Hash BCrypt generado
     */
    @PostMapping("/generate-hash-temp")
    public ResponseEntity<Map<String, String>> generateHashTemp(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        if (password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Parámetro 'password' requerido"));
        }
        
        // Usar el PasswordEncoder del contexto de Spring
        org.springframework.security.crypto.password.PasswordEncoder passwordEncoder = 
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        
        String hash = passwordEncoder.encode(password);
        
        log.info("🔐 Hash BCrypt generado para contraseña: {}", password);
        log.info("🔐 Hash resultante: {}", hash);
        
        return ResponseEntity.ok(Map.of(
            "password", password,
            "hash", hash,
            "message", "Hash generado exitosamente"
        ));
    }

    /**
     * Login JWT para usuarios locales
     * @param authorizationHeader Header de autorización con credenciales Basic
     * @return Token JWT
     */
    @PostMapping("/login")
    public ResponseJWTDTO login(@RequestHeader("Authorization") String authorizationHeader) {
        log.debug("Iniciando login JWT");
        
        if (authorizationHeader == null || !authorizationHeader.startsWith("Basic ")) {
            log.error("Header de autorización inválido");
            throw new UnauthorizedException("Header autenticación invalido");
        }
        
        try {
            String base64Credenciales = authorizationHeader.substring("Basic ".length());
            byte[] decodedBytesCredenciales = Base64.getDecoder().decode(base64Credenciales);
            String decodedCredenciales = new String(decodedBytesCredenciales);
            String[] partes = decodedCredenciales.split(":", 2);
            
            if (partes.length != 2) {
                log.error("Credenciales inválidas - formato incorrecto");
                throw new UnauthorizedException("Credenciales invalidas");
            }
            
            String email = partes[0];
            String password = partes[1];
            
            log.debug("Validando credenciales para email: {}", email);
            
            // Buscar usuario en la base de datos
            Usuario usuario = usuarioService.obtenerUsuarioPorEmailYPassword(email, password);
            if (usuario == null) {
                log.error("Usuario no encontrado o credenciales incorrectas para: {}", email);
                throw new UnauthorizedException("Usuario o contraseña incorrectos");
            }
            
            log.debug("Usuario autenticado exitosamente: {}", email);
            
            // Generar token JWT
            String token = jwtTokenProvider.generateJwtToken(usuario);
            log.debug("Token JWT generado exitosamente");
            
            return new ResponseJWTDTO(token);
            
        } catch (Exception e) {
            log.error("Error durante el login: {}", e.getMessage());
            throw new UnauthorizedException("Error en la autenticación: " + e.getMessage());
        }
    }

    /**
     * Valida credenciales de usuario mock
     * @param userDTO Datos del usuario
     * @return Usuario si es válido, null en caso contrario
     */
    private UserDTO validarLoginUser(UserDTO userDTO) {
        for (UserDTO user : usuariosMock()) {
            if (user.getUser().equals(userDTO.getUser()) && user.getPassword().equals(userDTO.getPassword())) {
                return user;
            }
        }
        return null;
    }

    /**
     * Obtiene usuario por ID mock
     * @param id ID del usuario
     * @return Usuario si existe, null en caso contrario
     */
    private UserDTO obtenerUserId(Long id) {
        for (UserDTO user : usuariosMock()) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Lista de usuarios mock para compatibilidad
     * @return Lista de usuarios de prueba
     */
    private List<UserDTO> usuariosMock() {
        List<UserDTO> lista = new ArrayList<>();

        List<String> permisosSeguridad = Arrays.asList("SEGURIDAD ESCRITURA", "SEGURIDAD LECTURA");
        List<String> permisosLectura = Arrays.asList("PROCESOS LECTURA", "TYC LECTURA");
        List<String> permisosModificacion = Arrays.asList("PROCESOS ESCRITURA", "TYC ESCRITURA");

        UserDTO seguridad = UserDTO.builder().id(1L).user("seguridad").password("12345678").permisos(permisosSeguridad).build();
        UserDTO lectura = UserDTO.builder().id(2L).user("lectura").password("1234").permisos(permisosLectura).build();
        UserDTO modificacion = UserDTO.builder().id(3L).user("modificacion").password("12345").permisos(permisosModificacion).build();

        lista.add(seguridad);
        lista.add(lectura);
        lista.add(modificacion);

        return lista;
    }
}
