# 🔐 Integración JWT + Azure AD

Este proyecto ahora soporta **dos tipos de autenticación**:

1. **Azure AD** (Microsoft Entra ID) - Para usuarios corporativos
2. **JWT Local** - Para usuarios de base de datos

## 🚀 Características

### ✅ Autenticación Dual
- **Azure AD**: Usuarios corporativos con grupos y permisos de Azure
- **JWT Local**: Usuarios locales con contraseñas en base de datos
- **Filtro Inteligente**: Detecta automáticamente el tipo de token

### ✅ Sistema de Permisos Unificado
- Mismos permisos para ambos tipos de usuarios
- Perfiles y roles consistentes
- Autorización basada en Spring Security

### ✅ Endpoints de Autenticación
- `POST /api/auth/login` - Login JWT local
- `GET /api/auth/info` - Información pública
- `GET /api/data/protected` - Datos protegidos

## 🛠️ Configuración

### 1. Dependencias Agregadas
```xml
<!-- Auth0 JWT -->
<dependency>
    <groupId>com.auth0</groupId>
    <artifactId>java-jwt</artifactId>
    <version>4.4.0</version>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

### 2. Propiedades de Configuración
```properties
# JWT Configuration (para autenticación local)
jwt.secret=mySecretKeyForJWTTokenGeneration123456789012345678901234567890
jwt.expiration=86400000
```

### 3. Usuarios de Prueba
Se han agregado usuarios locales con contraseñas:

| Email | Contraseña | Perfil |
|-------|------------|--------|
| admin@local.com | admin123 | Usuario Básico |
| user@local.com | user123 | Usuario Básico |
| guest@local.com | guest123 | Usuario Básico |

## 🔧 Uso

### Autenticación JWT Local

#### 1. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Authorization: Basic $(echo -n 'admin@local.com:admin123' | base64)" \
  -H "Content-Type: application/json"
```

**Respuesta:**
```json
{
  "token": "Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "message": "Autenticación exitosa"
}
```

#### 2. Usar Token
```bash
curl -X GET http://localhost:8080/api/data/protected \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9..."
```

### Autenticación Azure AD

El sistema Azure AD existente sigue funcionando igual. Los usuarios de Azure AD pueden autenticarse usando el frontend Angular con la configuración existente.

## 🏗️ Arquitectura

### Filtros de Seguridad
1. **DualAuthenticationFilter**: Detecta el tipo de token
2. **OAuth2ResourceServer**: Procesa tokens de Azure AD
3. **JWTTokenProvider**: Maneja tokens JWT locales

### Flujo de Autenticación
```
Request → DualAuthenticationFilter → 
  ├─ Token JWT Local → JWTTokenProvider → Spring Security Context
  └─ Token Azure AD → OAuth2ResourceServer → Spring Security Context
```

### Entidades
- **UsuarioEntity**: Wrapper para compatibilidad JWT
- **PermisoEntity**: Wrapper para permisos JWT
- **Usuario**: Entidad principal (modificada con campo password)

## 🧪 Pruebas

### Script de Prueba Automatizado
```powershell
.\test-jwt-integration.ps1
```

### Pruebas Manuales

#### 1. Probar Login JWT
```bash
# Crear credenciales Base64
echo -n "admin@local.com:admin123" | base64

# Hacer login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Authorization: Basic YWRtaW5AbG9jYWwuY29tOmFkbWluMTIz" \
  -H "Content-Type: application/json"
```

#### 2. Probar Endpoint Protegido
```bash
# Usar el token obtenido
curl -X GET http://localhost:8080/api/data/protected \
  -H "Authorization: Bearer TU_TOKEN_AQUI"
```

## 🔍 Monitoreo y Logs

### Logs de Autenticación
```properties
logging.level.com.example.apiprotegida.security=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Endpoints de Monitoreo
- `GET /api/actuator/health` - Estado de la aplicación
- `GET /api/actuator/info` - Información de la aplicación

## 🚨 Solución de Problemas

### Error: "Token no se puede verificar"
- Verificar que la clave secreta JWT esté configurada
- Verificar que el token no esté expirado

### Error: "Usuario o contraseña incorrectos"
- Verificar que el usuario exista en la base de datos
- Verificar que la contraseña sea correcta
- Verificar que el usuario esté activo

### Error: "Header autenticación invalido"
- Verificar que el header Authorization tenga el formato "Basic base64(usuario:contraseña)"
- Verificar que las credenciales estén correctamente codificadas en Base64

## 📚 Documentación Adicional

- [Spring Security JWT](https://spring.io/guides/tutorials/spring-security-and-angular-js/)
- [Auth0 JWT Java](https://github.com/auth0/java-jwt)
- [Azure AD Spring Boot](https://docs.microsoft.com/en-us/azure/developer/java/spring-framework/spring-boot-starter-for-azure-active-directory)

## 🎯 Próximos Pasos

1. **Configurar perfiles específicos** para usuarios JWT locales
2. **Implementar refresh tokens** para renovación automática
3. **Agregar validación de contraseñas** más robusta
4. **Implementar rate limiting** para endpoints de login
5. **Agregar auditoría** de autenticaciones
