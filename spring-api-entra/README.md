# 🚀 API Protegida con Spring Boot y Microsoft Entra ID

API REST segura desarrollada con Spring Boot que se integra con Microsoft Entra ID (Azure AD) para autenticación y autorización, diseñada para trabajar con aplicaciones Angular.

## 📋 Características

- ✅ **Autenticación JWT** con Microsoft Entra ID
- ✅ **CORS configurado** para Angular (`http://localhost:4200`)
- ✅ **Autorización basada en scopes** (`access_as_user`)
- ✅ **Base de datos H2** en memoria para desarrollo
- ✅ **Endpoints de ejemplo** para demostrar la integración
- ✅ **Actuator** para monitoring y health checks
- ✅ **Datos de prueba** precargados
- ✅ **Documentación completa** de endpoints

## 🔧 Configuración

### Credenciales ya configuradas:
- **Client ID**: `4a12fbd8-bf63-4c12-be4c-9678b207fbe7`
- **Tenant ID**: `f128ae87-3797-42d7-8490-82c6b570f832`
- **App ID URI**: `api://4a12fbd8-bf63-4c12-be4c-9678b207fbe7`
- **Scope**: `access_as_user`

## 🚀 Inicio Rápido

### Prerrequisitos
- Java 17 o superior
- Maven 3.6+
- Una aplicación registrada en Azure AD (ya configurada)

### Ejecutar la aplicación

#### Opción 1: Script de PowerShell (Recomendado)
```powershell
.\run-api.ps1
```

#### Opción 2: Script de Batch
```cmd
run-api.bat
```

#### Opción 3: Maven directo
```bash
mvn clean spring-boot:run
```

### URLs importantes una vez iniciada:
- 📡 **API Base**: `http://localhost:8080/api`
- 💾 **H2 Console**: `http://localhost:8080/api/h2-console`
- 📊 **Health Check**: `http://localhost:8080/api/actuator/health`
- ℹ️ **API Info**: `http://localhost:8080/api/auth/info`

## 📚 Endpoints Disponibles

### 🔓 Públicos (sin autenticación)
```http
GET /api/auth/info           # Información de la API
GET /api/actuator/health     # Estado de la aplicación
GET /api/h2-console          # Consola de base de datos
```

### 🔒 Protegidos (requieren autenticación)

#### Autenticación
```http
GET /api/auth/user-info      # Información del usuario autenticado
GET /api/auth/test           # Test de autenticación
GET /api/auth/token-claims   # Claims del JWT token
GET /api/auth/verify-scope   # Verificar scope específico
```

#### Usuarios
```http
GET    /api/users                    # Listar todos los usuarios
GET    /api/users/{id}               # Obtener usuario por ID
POST   /api/users                    # Crear nuevo usuario
PUT    /api/users/{id}               # Actualizar usuario
DELETE /api/users/{id}               # Eliminar usuario
GET    /api/users/mi-perfil          # Perfil del usuario actual
POST   /api/users/mi-perfil          # Crear/actualizar perfil
GET    /api/users/estadisticas       # Estadísticas de usuarios
GET    /api/users/departamento/{dept} # Usuarios por departamento
GET    /api/users/buscar?nombre=X    # Buscar usuarios por nombre
```

#### Datos
```http
GET  /api/data                    # Datos básicos protegidos
GET  /api/data/dashboard          # Datos para dashboard
GET  /api/data/proceso-lento      # Simula proceso que toma tiempo
GET  /api/data/config             # Configuración de la aplicación
GET  /api/data/reportes/{tipo}    # Reportes (ventas, usuarios, productos)
GET  /api/data/health             # Estado de servicios
POST /api/data                    # Crear datos
```

## 🗄️ Base de Datos

### Conexión H2
- **URL**: `jdbc:h2:mem:testdb`
- **Usuario**: `sa`
- **Contraseña**: `password`
- **Console**: `http://localhost:8080/api/h2-console`

### Datos de Ejemplo
La aplicación incluye 10 usuarios de ejemplo con diferentes departamentos:
- Desarrollo, Diseño, Marketing, Operaciones, Recursos Humanos, Finanzas, Ventas

## 🔐 Seguridad

### Configuración JWT
- **Issuer**: `https://login.microsoftonline.com/f128ae87-3797-42d7-8490-82c6b570f832/v2.0`
- **Audience**: `api://4a12fbd8-bf63-4c12-be4c-9678b207fbe7`
- **Scope requerido**: `access_as_user`

### CORS
Configurado para permitir requests desde:
- `http://localhost:4200` (Angular dev server)
- `https://localhost:4200` (Angular dev server con HTTPS)

## 🧪 Pruebas

### Con curl (requiere token JWT)
```bash
# Obtener información de la API (público)
curl http://localhost:8080/api/auth/info

# Test de autenticación (requiere token)
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8080/api/auth/test

# Obtener datos protegidos
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8080/api/data
```

### Con Angular
1. Inicia la aplicación Angular: `npm start`
2. Ve a `http://localhost:4200`
3. Inicia sesión con Microsoft
4. Prueba los diferentes botones para llamar a la API

## 📁 Estructura del Proyecto

```
spring-api-entra/
├── src/main/java/com/example/apiprotegida/
│   ├── ApiProtegidaApplication.java    # Clase principal
│   ├── config/
│   │   └── SecurityConfig.java         # Configuración de seguridad
│   ├── controller/
│   │   ├── AuthController.java         # Endpoints de autenticación
│   │   ├── DataController.java         # Endpoints de datos
│   │   └── UsuarioController.java      # CRUD de usuarios
│   ├── model/
│   │   └── Usuario.java                # Entidad Usuario
│   └── repository/
│       └── UsuarioRepository.java      # Repositorio JPA
├── src/main/resources/
│   ├── application.yml                 # Configuración principal
│   └── data.sql                        # Datos de ejemplo
├── pom.xml                             # Dependencias Maven
├── run-api.ps1                         # Script PowerShell
├── run-api.bat                         # Script Batch
└── README.md                           # Este archivo
```

## 🔧 Configuración Avanzada

### Variables de Entorno
Puedes sobrescribir la configuración usando variables de entorno:

```bash
export AZURE_TENANT_ID=tu-tenant-id
export AZURE_CLIENT_ID=tu-client-id
export AZURE_CLIENT_SECRET=tu-client-secret
export SERVER_PORT=8080
```

### Perfiles de Spring
```bash
# Desarrollo (por defecto)
mvn spring-boot:run

# Producción
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 🐛 Troubleshooting

### Error: "Invalid JWT token"
- Verifica que el token no haya expirado
- Confirma que el scope `access_as_user` esté incluido
- Revisa que la aplicación Angular esté enviando el header `Authorization`

### Error: "CORS policy"
- Verifica que Angular esté corriendo en `http://localhost:4200`
- Revisa la configuración CORS en `SecurityConfig.java`

### Error: "Access denied"
- Confirma que el usuario tenga el scope correcto
- Verifica que la aplicación esté registrada correctamente en Azure AD

## 📞 Soporte

Si encuentras problemas:
1. Revisa los logs de la aplicación
2. Verifica la configuración en `application.yml`
3. Confirma que Azure AD esté configurado correctamente
4. Revisa la consola del navegador para errores CORS

## 🎯 Próximos Pasos

1. **Personalizar endpoints** según tus necesidades
2. **Agregar más entidades** y controladores
3. **Implementar base de datos** persistente (PostgreSQL, MySQL)
4. **Agregar tests unitarios** y de integración
5. **Configurar CI/CD** para despliegue automático
6. **Documentar con Swagger** para mejor UX

¡Tu API Spring Boot está lista para integrarse con Angular! 🎉
