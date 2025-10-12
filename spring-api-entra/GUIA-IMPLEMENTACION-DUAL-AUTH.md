# 🎯 Guía de Implementación - Sistema Dual de Autenticación

## 📋 Resumen Ejecutivo

Este documento explica cómo implementar un **sistema dual de autenticación** que permite usar simultáneamente:
- **Azure AD (OAuth2/JWT)** - Para usuarios corporativos de Microsoft
- **JWT Local con Base de Datos** - Para usuarios locales con email/contraseña

Ambos sistemas **coexisten sin interferirse** y pueden ser **habilitados/deshabilitados dinámicamente** mediante flags en base de datos.

---

## 📦 **1. DEPENDENCIAS (pom.xml)**

### Agregar estas dependencias:

```xml
<!-- Auth0 JWT para tokens locales -->
<dependency>
    <groupId>com.auth0</groupId>
    <artifactId>java-jwt</artifactId>
    <version>4.4.0</version>
</dependency>

<!-- Lombok para reducir boilerplate -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- Spring Security OAuth2 Resource Server (para Azure AD) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Spring Cache (para performance) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

---

## ⚙️ **2. CONFIGURACIÓN (application.properties)**

```properties
# Azure AD Configuration
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://login.microsoftonline.com/{tenant-id}/v2.0
azure.activedirectory.tenant-id={tu-tenant-id}
azure.activedirectory.client-id={tu-client-id}

# JWT Local Configuration
jwt.secret=tuSecretoSuperSeguroParaJWT123456789012345678901234567890
jwt.expiration=86400000

# CORS Configuration
cors.allowed-origins=http://localhost:4200,https://localhost:4200

# H2 Database (opcional - para desarrollo)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

---

## 🗄️ **3. MODELO DE DATOS**

### A. Entidad Usuario

Agrega el campo `password` a tu entidad de usuarios:

```java
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private String email;
    
    // NUEVO: Campo para autenticación JWT local
    @Column(name = "password")
    private String password;
    
    private String azureObjectId; // Para Azure AD
    private Boolean activo = true;
    
    // Getters y Setters
}
```

### B. Entidad ConfiguracionSistema (NUEVA)

```java
@Entity
@Table(name = "configuracion_sistema")
@Data
public class ConfiguracionSistema {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String clave;
    
    @Column(nullable = false)
    private String valor;
    
    private String descripcion;
    private String tipo; // BOOLEAN, STRING, NUMBER
    private String categoria; // AUTENTICACION, SEGURIDAD, GENERAL
    private Boolean activo = true;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    public Boolean getValorBoolean() {
        return Boolean.parseBoolean(valor);
    }
}
```

### C. Script SQL (data.sql)

```sql
-- Tabla de configuración
CREATE TABLE IF NOT EXISTS configuracion_sistema (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    clave VARCHAR(100) UNIQUE NOT NULL,
    valor VARCHAR(500) NOT NULL,
    descripcion VARCHAR(500),
    tipo VARCHAR(50),
    categoria VARCHAR(50),
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP,
    fecha_actualizacion TIMESTAMP
);

-- Flags de autenticación
INSERT INTO configuracion_sistema (clave, valor, descripcion, tipo, categoria, activo, fecha_creacion, fecha_actualizacion)
VALUES ('auth.azure.enabled', 'true', 'Habilita Azure AD', 'BOOLEAN', 'AUTENTICACION', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO configuracion_sistema (clave, valor, descripcion, tipo, categoria, activo, fecha_creacion, fecha_actualizacion)
VALUES ('auth.jwt.local.enabled', 'true', 'Habilita JWT Local', 'BOOLEAN', 'AUTENTICACION', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Usuarios locales con contraseñas BCrypt
INSERT INTO usuarios (nombre, email, password, activo, fecha_creacion, fecha_actualizacion)
VALUES ('Admin', 'admin@local.com', '$2a$10$hash-bcrypt-aqui', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
```

---

## 🔐 **4. SEGURIDAD - COMPONENTES PRINCIPALES**

### A. SecurityConstant.java (NUEVO)

```java
public class SecurityConstant {
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String AUTHORITIES = "authorities";
    public static final long EXPIRATION_TIME = 86_400_000; // 24 horas
    public static final String API_TYC = "API-TYC";
    public static final String TOKEN_NO_SE_PUEDE_VERIFICAR = "El token no se puede verificar";
}
```

### B. JWTTokenProvider.java (NUEVO)

```java
@Component
@Slf4j
public class JWTTokenProvider {
    
    @Value("${jwt.secret}")
    private String secret;

    // Genera token JWT para usuario local
    public String generateJwtToken(Usuario usuario) {
        List<String> authorities = List.of("USUARIOS_LEER", "DASHBOARD_LEER");
        
        return TOKEN_PREFIX + JWT.create()
                .withIssuer(API_TYC)
                .withSubject(usuario.getEmail())
                .withArrayClaim(AUTHORITIES, authorities.toArray(new String[0]))
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(secret.getBytes()));
    }

    // Extrae autoridades del token
    public List<GrantedAuthority> getAuthorities(String token) {
        String[] claims = getClaimsFromToken(token).getClaim(AUTHORITIES).asArray(String.class);
        return Arrays.stream(claims)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    // Valida el token
    public boolean isTokenValid(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC512(secret.getBytes())).build();
            verifier.verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Obtiene subject (email)
    public String getSubject(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    // Crea Authentication
    public Authentication getAuthentication(String username, List<GrantedAuthority> authorities, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken(username, null, authorities);
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authToken;
    }
}
```

### C. DualAuthenticationFilter.java (NUEVO - MUY IMPORTANTE)

```java
@Component
@Slf4j
public class DualAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JWTTokenProvider jwtTokenProvider;
    
    @Autowired
    private ConfiguracionService configuracionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Solo procesar si no hay autenticación previa
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String authorizationHeader = request.getHeader(AUTHORIZATION);
            
            if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
                String token = authorizationHeader.substring(TOKEN_PREFIX.length());
                
                // Distinguir entre JWT local y Azure AD
                if (isLocalJwtToken(token)) {
                    // Verificar flag de JWT local
                    if (!configuracionService.esJwtLocalHabilitado()) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("{\"error\":\"Autenticación JWT local deshabilitada\"}");
                        response.setContentType("application/json");
                        return;
                    }
                    
                    // Procesar JWT local
                    processLocalJwtToken(token, request);
                    request.setAttribute("JWT_LOCAL_PROCESSED", true);
                    
                } else {
                    // Verificar flag de Azure AD
                    if (!configuracionService.esAzureAdHabilitado()) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("{\"error\":\"Autenticación Azure AD deshabilitada\"}");
                        response.setContentType("application/json");
                        return;
                    }
                    
                    // Delegar a OAuth2 Resource Server
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }

    private boolean isLocalJwtToken(String token) {
        try {
            return jwtTokenProvider.isTokenValid(token);
        } catch (Exception e) {
            return false;
        }
    }

    private void processLocalJwtToken(String token, HttpServletRequest request) {
        if (jwtTokenProvider.isTokenValid(token)) {
            String subject = jwtTokenProvider.getSubject(token);
            List<GrantedAuthority> authorities = jwtTokenProvider.getAuthorities(token);
            
            Authentication authentication = jwtTokenProvider.getAuthentication(subject, authorities, request);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }
}
```

---

## 🔧 **5. CONFIGURACIÓN DE SEGURIDAD (SecurityConfig.java)**

### Configuración Completa:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private DualAuthenticationFilter dualAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(
                    "/auth/info",
                    "/auth/login",
                    "/config/auth/status"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .bearerTokenResolver(customBearerTokenResolver())
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .addFilterBefore(dualAuthenticationFilter, BearerTokenAuthenticationFilter.class)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    // BearerTokenResolver personalizado que ignora JWT locales ya procesados
    @Bean
    public BearerTokenResolver customBearerTokenResolver() {
        DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();
        
        return new BearerTokenResolver() {
            @Override
            public String resolve(HttpServletRequest request) {
                // Si ya procesamos un JWT local, no resolver
                if (request.getAttribute("JWT_LOCAL_PROCESSED") != null) {
                    return null;
                }
                
                // Si ya hay autenticación, no procesar
                if (SecurityContextHolder.getContext().getAuthentication() != null && 
                    SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
                    return null;
                }
                
                return resolver.resolve(request);
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
    }
}
```

---

## 🎛️ **6. SERVICIO DE CONFIGURACIÓN**

### ConfiguracionService.java

```java
@Service
@Slf4j
public class ConfiguracionService {
    
    @Autowired
    private ConfiguracionSistemaRepository configuracionRepository;
    
    public static final String AUTH_AZURE_AD_ENABLED = "auth.azure.enabled";
    public static final String AUTH_JWT_LOCAL_ENABLED = "auth.jwt.local.enabled";
    
    @Cacheable(value = "configuracion", key = "#clave")
    @Transactional(readOnly = true)
    public Optional<ConfiguracionSistema> obtenerPorClave(String clave) {
        return configuracionRepository.findByClave(clave);
    }
    
    @Transactional(readOnly = true)
    public Boolean obtenerValorBoolean(String clave, Boolean valorPorDefecto) {
        return obtenerPorClave(clave)
                .map(ConfiguracionSistema::getValorBoolean)
                .orElse(valorPorDefecto);
    }
    
    public boolean esAzureAdHabilitado() {
        return obtenerValorBoolean(AUTH_AZURE_AD_ENABLED, true);
    }
    
    public boolean esJwtLocalHabilitado() {
        return obtenerValorBoolean(AUTH_JWT_LOCAL_ENABLED, true);
    }
    
    @CacheEvict(value = "configuracion", key = "...")
    @Transactional
    public void establecerAzureAdHabilitado(boolean habilitado) {
        actualizarValor(AUTH_AZURE_AD_ENABLED, String.valueOf(habilitado));
    }
    
    @CacheEvict(value = "configuracion", key = "...")
    @Transactional
    public void establecerJwtLocalHabilitado(boolean habilitado) {
        actualizarValor(AUTH_JWT_LOCAL_ENABLED, String.valueOf(habilitado));
    }
}
```

---

## 🎮 **7. CONTROLADORES**

### A. BFFUserController.java - Login JWT Local

```java
@RestController
@RequestMapping("/auth")
@Slf4j
public class BFFUserController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private JWTTokenProvider jwtTokenProvider;
    
    @PostMapping("/login")
    public ResponseJWTDTO login(@RequestHeader("Authorization") String authorizationHeader) {
        // Validar header Basic Auth
        if (authorizationHeader == null || !authorizationHeader.startsWith("Basic ")) {
            throw new UnauthorizedException("Header autenticación inválido");
        }
        
        // Decodificar credenciales
        String base64Credenciales = authorizationHeader.substring("Basic ".length());
        byte[] decodedBytes = Base64.getDecoder().decode(base64Credenciales);
        String decodedCredenciales = new String(decodedBytes);
        String[] partes = decodedCredenciales.split(":", 2);
        
        String email = partes[0];
        String password = partes[1];
        
        // Validar credenciales en BD
        Usuario usuario = usuarioService.obtenerUsuarioPorEmailYPassword(email, password);
        if (usuario == null) {
            throw new UnauthorizedException("Usuario o contraseña incorrectos");
        }
        
        // Generar token JWT
        String token = jwtTokenProvider.generateJwtToken(usuario);
        return new ResponseJWTDTO(token);
    }
}
```

### B. ConfiguracionController.java - Gestión de Flags

```java
@RestController
@RequestMapping("/config")
@Slf4j
public class ConfiguracionController {
    
    @Autowired
    private ConfiguracionService configuracionService;
    
    // Endpoint PÚBLICO para consultar estado
    @GetMapping("/auth/status")
    public ResponseEntity<Map<String, Object>> obtenerEstadoAutenticacion() {
        Map<String, Object> status = new HashMap<>();
        status.put("azureAdHabilitado", configuracionService.esAzureAdHabilitado());
        status.put("jwtLocalHabilitado", configuracionService.esJwtLocalHabilitado());
        return ResponseEntity.ok(status);
    }
    
    // Endpoint PROTEGIDO para cambiar flags
    @PostMapping("/auth/azure/toggle")
    @PreAuthorize("hasAnyAuthority('SCOPE_access_as_user', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> toggleAzureAd(@RequestBody Map<String, Boolean> request) {
        Boolean habilitado = request.get("habilitado");
        configuracionService.establecerAzureAdHabilitado(habilitado);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Azure AD " + (habilitado ? "habilitado" : "deshabilitado"));
        response.put("azureAdHabilitado", habilitado);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/auth/jwt-local/toggle")
    @PreAuthorize("hasAnyAuthority('SCOPE_access_as_user', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> toggleJwtLocal(@RequestBody Map<String, Boolean> request) {
        Boolean habilitado = request.get("habilitado");
        configuracionService.establecerJwtLocalHabilitado(habilitado);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "JWT Local " + (habilitado ? "habilitado" : "deshabilitado"));
        response.put("jwtLocalHabilitado", habilitado);
        return ResponseEntity.ok(response);
    }
}
```

---

## 🔨 **8. SERVICIOS**

### UsuarioService.java

```java
@Service
@Slf4j
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorEmailYPassword(String email, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        
        if (usuarioOpt.isEmpty()) {
            log.warn("Usuario no encontrado: {}", email);
            return null;
        }
        
        Usuario usuario = usuarioOpt.get();
        
        if (!usuario.getActivo()) {
            log.warn("Usuario inactivo: {}", email);
            return null;
        }
        
        if (usuario.getPassword() == null || !passwordEncoder.matches(password, usuario.getPassword())) {
            log.warn("Contraseña incorrecta para: {}", email);
            return null;
        }
        
        return usuario;
    }
}
```

---

## 🎯 **9. ANOTACIONES DE SEGURIDAD PERSONALIZADAS**

### RoleAnnotations.java

```java
public class RoleAnnotations {
    
    // Compatible con Azure AD Y JWT Local
    @PreAuthorize("hasAuthority('SCOPE_access_as_user') or hasAnyAuthority('USUARIOS_LEER', 'DASHBOARD_LEER', 'ADMIN')")
    public @interface ValidScope {}
    
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public @interface AdminManagerOrUser {}
    
    @PreAuthorize("hasRole('ADMIN')")
    public @interface AdminOnly {}
}
```

**USO:**
```java
@GetMapping("/data")
@RoleAnnotations.ValidScope  // Acepta Azure AD O JWT Local
public ResponseEntity<Data> getData() {
    // ...
}
```

---

## 🚀 **10. CLASE PRINCIPAL**

```java
@SpringBootApplication
@EntityScan("com.tupackage.model")
@EnableJpaRepositories("com.tupackage.repository")
@EnableCaching  // Importante para performance
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

---

## 📊 **11. FLUJO DE AUTENTICACIÓN**

### Para Azure AD:
```
1. Cliente → Token Azure AD
2. DualAuthenticationFilter → Detecta que NO es JWT local
3. DualAuthenticationFilter → Verifica flag auth.azure.enabled
4. Si enabled=false → 403 Forbidden
5. Si enabled=true → Delega a BearerTokenAuthenticationFilter
6. OAuth2 Resource Server → Valida con Microsoft
7. Autenticación establecida ✅
```

### Para JWT Local:
```
1. Cliente → POST /auth/login con Basic Auth
2. BFFUserController → Valida credenciales en BD
3. JWTTokenProvider → Genera token JWT
4. Cliente → Recibe token

Uso del token:
1. Cliente → Request con Bearer token
2. DualAuthenticationFilter → Detecta que ES JWT local
3. DualAuthenticationFilter → Verifica flag auth.jwt.local.enabled
4. Si enabled=false → 403 Forbidden
5. Si enabled=true → Valida token
6. Marca request como "JWT_LOCAL_PROCESSED"
7. BearerTokenResolver → Ignora (ya procesado)
8. Autenticación establecida ✅
```

---

## 🎨 **12. PUNTOS CLAVE DE LA ARQUITECTURA**

### ✅ **Orden de Filtros Crítico:**

```
1. DualAuthenticationFilter ← PRIMERO (procesa JWT local)
2. BearerTokenAuthenticationFilter ← SEGUNDO (procesa Azure AD)
```

Si el orden está mal, el sistema NO funciona.

### ✅ **BearerTokenResolver Personalizado:**

```java
// CRÍTICO: Evita que OAuth2 procese tokens JWT locales
if (request.getAttribute("JWT_LOCAL_PROCESSED") != null) {
    return null; // No procesar
}
```

### ✅ **Caché para Performance:**

```java
@Cacheable(value = "configuracion", key = "#clave")
public Optional<ConfiguracionSistema> obtenerPorClave(String clave) {
    // Solo consulta BD una vez, luego usa caché
}

@CacheEvict(value = "configuracion", key = "...")
public void actualizarValor(...) {
    // Invalida caché al actualizar
}
```

### ✅ **Validación de Contraseñas:**

```java
// Usar BCrypt con strength 10
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hashedPassword = encoder.encode("password");

// Generar hash: https://bcrypt-generator.com/
// Validar: passwordEncoder.matches(plainText, hash)
```

---

## 📝 **13. CHECKLIST DE IMPLEMENTACIÓN**

### Fase 1: Dependencias y Configuración
- [ ] Agregar dependencias en `pom.xml`
- [ ] Configurar `application.properties`
- [ ] Agregar campo `password` a entidad Usuario
- [ ] Crear entidad `ConfiguracionSistema`

### Fase 2: Seguridad
- [ ] Crear `SecurityConstant`
- [ ] Crear `JWTTokenProvider`
- [ ] Crear `DualAuthenticationFilter`
- [ ] Configurar `SecurityConfig` con orden de filtros correcto
- [ ] Crear `BearerTokenResolver` personalizado

### Fase 3: Servicios y Repositorios
- [ ] Crear `ConfiguracionSistemaRepository`
- [ ] Crear `ConfiguracionService` con caché
- [ ] Actualizar `UsuarioService` para validar credenciales

### Fase 4: Controladores
- [ ] Crear `BFFUserController` con endpoint `/login`
- [ ] Crear `ConfiguracionController` para gestionar flags
- [ ] Actualizar anotaciones de seguridad (`RoleAnnotations`)

### Fase 5: Base de Datos
- [ ] Crear tabla `configuracion_sistema` en schema
- [ ] Insertar flags iniciales en `data.sql`
- [ ] Insertar usuarios de prueba con contraseñas BCrypt

### Fase 6: Testing
- [ ] Probar login Azure AD
- [ ] Probar login JWT local
- [ ] Probar deshabilitar Azure AD
- [ ] Probar deshabilitar JWT local
- [ ] Probar ambos habilitados/deshabilitados

---

## 🚨 **14. ERRORES COMUNES Y SOLUCIONES**

### Error 1: "JWT was invalid" con tokens locales

**Causa:** `BearerTokenAuthenticationFilter` intenta validar JWT local como Azure AD

**Solución:** 
- Asegurar que `DualAuthenticationFilter` esté ANTES
- Implementar `BearerTokenResolver` personalizado
- Marcar request con atributo "JWT_LOCAL_PROCESSED"

### Error 2: "Access Denied" con usuarios JWT

**Causa:** Anotaciones `@PreAuthorize` solo aceptan scopes de Azure AD

**Solución:**
```java
// ANTES
@PreAuthorize("hasAuthority('SCOPE_access_as_user')")

// DESPUÉS (compatible con ambos)
@PreAuthorize("hasAuthority('SCOPE_access_as_user') or hasAnyAuthority('USUARIOS_LEER', 'DASHBOARD_LEER')")
```

### Error 3: Contraseña siempre incorrecta

**Causa:** Hash BCrypt inválido en base de datos

**Solución:**
- Generar hash con: https://bcrypt-generator.com/
- O crear endpoint temporal para generar hashes
- Usar strength 10 para BCrypt

### Error 4: SecurityContext limpiado incorrectamente

**Causa:** Múltiples filtros limpiando el contexto

**Solución:**
- NO llamar `SecurityContextHolder.clearContext()` si puede ser token de otro tipo
- Verificar autenticación antes de limpiar

---

## 💡 **15. MEJORES PRÁCTICAS**

### ✅ Logging Detallado
```java
log.info("🔍 [Service] Buscando usuario: {}", email);
log.info("✅ [Service] Usuario encontrado");
log.warn("❌ [Service] Contraseña incorrecta");
```

### ✅ Manejo de Errores
```java
if (authentication == null || !authentication.isAuthenticated()) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "No autenticado"));
}
```

### ✅ DTOs para Responses
```java
@Data
@AllArgsConstructor
public class ResponseJWTDTO {
    private String jwt;
}
```

### ✅ Validaciones en Service Layer
```java
// NUNCA en Controller, siempre en Service
public Usuario validarCredenciales(String email, String password) {
    // Validaciones aquí
}
```

---

## 📐 **16. ARQUITECTURA DEL SISTEMA**

```
┌─────────────────────────────────────────────────────┐
│                  CLIENTE (Angular/Postman)          │
└───────────────────┬─────────────────────────────────┘
                    │
        ┌───────────┴────────────┐
        │                        │
   Token Azure AD          Token JWT Local
        │                        │
        └───────────┬────────────┘
                    ▼
┌─────────────────────────────────────────────────────┐
│          DualAuthenticationFilter                   │
│  • Distingue tipo de token                          │
│  • Verifica flags en BD                             │
│  • Procesa JWT local O delega Azure AD              │
└───────────────────┬─────────────────────────────────┘
                    │
        ┌───────────┴────────────┐
        │                        │
  JWT Local OK           Azure AD → OAuth2 Server
        │                        │
        └───────────┬────────────┘
                    ▼
┌─────────────────────────────────────────────────────┐
│           SecurityContextHolder                      │
│  • Authentication establecida                       │
│  • Authorities cargadas                             │
└───────────────────┬─────────────────────────────────┘
                    ▼
┌─────────────────────────────────────────────────────┐
│              @PreAuthorize                          │
│  • Valida authorities                               │
│  • Compatible con ambos sistemas                    │
└───────────────────┬─────────────────────────────────┘
                    ▼
                ENDPOINT
```

---

## 🎯 **17. ENDPOINTS RESULTANTES**

### Públicos (Sin Auth):
```
GET  /auth/info
POST /auth/login
GET  /config/auth/status
```

### Protegidos (Con Auth):
```
GET  /data
GET  /users
POST /config/auth/azure/toggle
POST /config/auth/jwt-local/toggle
GET  /config/auth/all
```

---

## 📦 **18. ESTRUCTURA DE ARCHIVOS**

```
src/main/java/com/example/
├── config/
│   └── SecurityConfig.java              ← Configuración dual
├── controller/
│   ├── BFFUserController.java           ← Login JWT
│   └── ConfiguracionController.java     ← Gestión flags
├── model/
│   ├── Usuario.java                     ← Con campo password
│   └── ConfiguracionSistema.java        ← NUEVO
├── repository/
│   └── ConfiguracionSistemaRepository.java ← NUEVO
├── security/
│   ├── JWTTokenProvider.java            ← NUEVO
│   ├── SecurityConstant.java            ← NUEVO
│   ├── RoleAnnotations.java             ← Actualizado
│   └── filter/
│       └── DualAuthenticationFilter.java ← NUEVO
├── service/
│   ├── UsuarioService.java              ← Actualizado
│   └── ConfiguracionService.java        ← NUEVO
└── exceptions/
    └── UnauthorizedException.java       ← NUEVO

src/main/resources/
├── application.properties               ← Configuración JWT
└── data.sql                             ← Tabla config + usuarios
```

---

## 🎓 **19. CONCEPTOS CLAVE**

### Spring Security Filter Chain
Los filtros se ejecutan en orden específico. El `DualAuthenticationFilter` **DEBE** estar antes del `BearerTokenAuthenticationFilter`.

### BearerTokenResolver
Determina SI un request tiene un token Bearer. Lo personalizamos para ignorar tokens ya procesados.

### @PreAuthorize
Valida autoridades DESPUÉS de la autenticación. Debe ser compatible con ambos sistemas.

### BCrypt
Algoritmo de hash unidireccional. **NUNCA** almacenar contraseñas en texto plano.

### Caché
Evita consultas repetidas a BD. Invalidar al actualizar para mantener consistencia.

---

## ✨ **20. RESULTADO FINAL**

Al implementar esta guía, tendrás:

✅ **Sistema dual funcionando** - Azure AD + JWT Local  
✅ **Control dinámico** - Habilitar/deshabilitar sin reiniciar  
✅ **Persistencia** - Flags en base de datos  
✅ **API completa** - Endpoints para gestión  
✅ **Seguridad robusta** - Validaciones en todos los niveles  
✅ **Performance** - Caché para configuraciones  
✅ **Logging detallado** - Debugging fácil  
✅ **Escalable** - Fácil agregar nuevos métodos de auth  

---

## 📞 **SOPORTE**

Si implementas esto en otro proyecto y tienes dudas:

1. Revisa los logs detallados (tienen emojis para fácil identificación)
2. Verifica el orden de filtros en SecurityConfig
3. Asegúrate de que BearerTokenResolver esté configurado
4. Confirma que las anotaciones @PreAuthorize sean compatibles
5. Valida que los hashes BCrypt sean correctos

---

**Creado por**: AI Assistant  
**Fecha**: Octubre 6, 2025  
**Versión**: 1.0 - Guía Completa de Implementación



