# 🔐 Guía de Autenticación Dual - Frontend Angular

## 📋 Resumen de Cambios

Se ha implementado exitosamente un sistema de autenticación dual en el frontend Angular que soporta:

1. **Autenticación con Microsoft Entra ID (Azure AD)** - Autenticación corporativa
2. **Autenticación Local con JWT** - Autenticación con usuario y contraseña de base de datos

## 🎯 Características Principales

### ✅ Detección Automática del Método de Autenticación

El frontend ahora consulta al backend al iniciar para determinar qué método de autenticación está activo. Solo un método puede estar habilitado a la vez.

### ✅ Flujos de Autenticación Separados

- **Azure AD**: Login mediante redirección a Microsoft con MSAL
- **JWT Local**: Login con formulario de usuario/contraseña

### ✅ Gestión Unificada de Permisos

Ambos métodos de autenticación comparten el mismo sistema de permisos y autorización.

## 📁 Archivos Creados

### Servicios

1. **`src/app/services/auth-config.service.ts`**
   - Consulta al backend el método de autenticación habilitado
   - Endpoint: `GET /api/config/auth/status`
   - Almacena la configuración en localStorage

2. **`src/app/services/local-auth.service.ts`**
   - Maneja login/logout con JWT
   - Almacena token JWT en localStorage
   - Decodifica y guarda información del usuario

### Componentes

3. **`src/app/components/auth-selector.component.ts`**
   - Componente que muestra las opciones de autenticación disponibles
   - Redirige automáticamente según la configuración del backend

4. **`src/app/components/local-login.component.ts`**
   - Formulario de login para autenticación local
   - Validación de email y contraseña
   - Feedback visual de errores

### Interceptores

5. **`src/app/interceptors/jwt-auth.interceptor.ts`**
   - Agrega automáticamente el token JWT a las peticiones HTTP
   - Solo se activa cuando la autenticación local está habilitada
   - Maneja errores 401/403 y redirige al login

### Guards

6. **`src/app/guards/auth.guard.ts`**
   - Guard unificado que funciona con ambos métodos de autenticación
   - Verifica autenticación según el método activo
   - Redirige al login apropiado si no está autenticado

## 🔄 Flujo de Autenticación

### Inicio de la Aplicación

1. La aplicación carga y consulta al backend: `GET /api/config/auth/status`
2. El backend responde con:
   ```json
   {
     "azureAdHabilitado": false,
     "jwtLocalHabilitado": true,
     "timestamp": 1234567890
   }
   ```
3. El frontend almacena la configuración y muestra la interfaz apropiada

### Autenticación con Azure AD

1. Usuario hace clic en "Iniciar sesión con Microsoft"
2. Redirección a Microsoft para autenticación
3. Microsoft redirige de vuelta con código de autorización
4. MSAL obtiene y almacena el token automáticamente
5. El frontend carga los permisos del usuario desde el backend

### Autenticación Local

1. Usuario accede a `/login`
2. Ingresa email y contraseña
3. Las credenciales se envían con Basic Auth: `POST /api/auth/login`
4. El backend valida y devuelve un token JWT:
   ```json
   {
     "token": "Bearer eyJhbGc...",
     "type": "Bearer",
     "message": "Login exitoso"
   }
   ```
5. El frontend almacena el token y decodifica la información del usuario
6. Se cargan los permisos del usuario desde el backend

## 🔧 Configuración

### Backend (Ya configurado)

El backend tiene estos endpoints disponibles:

- `GET /api/config/auth/status` - Obtiene el estado de autenticación (público)
- `POST /api/auth/login` - Login con usuario/contraseña
- `GET /api/autorizacion/informacion-usuario` - Obtiene permisos del usuario (requiere autenticación)

### Configuración del Backend

En `application.properties`:

```properties
# Habilitar/deshabilitar Azure AD
AZURE_AD_ENABLED=true

# Habilitar/deshabilitar JWT Local
JWT_LOCAL_ENABLED=true

# IMPORTANTE: Solo una debe estar habilitada a la vez
```

O mediante la API de administración:

```bash
# Cambiar a autenticación local
POST /api/config/auth/config/admin
Headers:
  X-Admin-Token: ADMIN_SECRET_TOKEN_2024
Body:
{
  "azureEnabled": false,
  "jwtLocalEnabled": true
}
```

## 🚀 Uso

### Para Usuarios Finales

1. **Acceder a la aplicación**: `http://localhost:4200`
2. Se mostrará automáticamente el método de autenticación disponible
3. Seguir el flujo de login correspondiente

### Para Desarrolladores

#### Verificar el Método de Autenticación Activo

```typescript
import { AuthConfigService } from './services/auth-config.service';

constructor(private authConfig: AuthConfigService) {}

ngOnInit() {
  const method = this.authConfig.getActiveAuthMethod();
  console.log('Método activo:', method); // 'azure' | 'local' | 'none'
}
```

#### Verificar si el Usuario Está Autenticado

```typescript
// Con Azure AD
const isAzureAuth = this.msalService.instance.getAllAccounts().length > 0;

// Con JWT Local
const isLocalAuth = this.localAuthService.isAuthenticated();

// O usar el método unificado en AppComponent
const isLoggedIn = this.isLoggedIn; // Ya maneja ambos casos
```

#### Obtener Información del Usuario

```typescript
// Con Azure AD
const accounts = this.msalService.instance.getAllAccounts();
const userName = accounts[0]?.name;

// Con JWT Local
const user = this.localAuthService.getCurrentUser();
const userName = user?.nombre;
```

## 📊 Endpoints del Backend

### Públicos (Sin autenticación)

- `GET /api/config/auth/status` - Estado de autenticación

### Protegidos (Requieren autenticación)

- `GET /api/autorizacion/informacion-usuario` - Información y permisos del usuario
- `GET /api/autorizacion/permisos` - Lista de permisos del usuario
- `GET /api/data/*` - Endpoints de datos protegidos

## 🔐 Seguridad

### Almacenamiento de Tokens

- **Azure AD**: Tokens almacenados por MSAL en localStorage
- **JWT Local**: Token almacenado en localStorage con key `local_jwt_token`

### Interceptores HTTP

Se utilizan dos interceptores en secuencia:

1. **MsalInterceptor**: Agrega tokens de Azure AD (solo si Azure está activo)
2. **JwtAuthInterceptor**: Agrega tokens JWT locales (solo si JWT está activo)

### Protección de Rutas

Todas las rutas protegidas usan `AuthGuard` que verifica autenticación según el método activo.

## 🧪 Pruebas

### Probar Autenticación Local

1. Configurar backend para JWT Local:
   ```bash
   # En application.properties
   JWT_LOCAL_ENABLED=true
   AZURE_AD_ENABLED=false
   ```

2. Reiniciar el backend

3. Acceder a `http://localhost:4200`

4. Usar credenciales de prueba del backend:
   - Email: `admin@test.com` o según usuarios en la BD
   - Contraseña: La configurada en el backend

### Probar Autenticación Azure

1. Configurar backend para Azure AD:
   ```bash
   # En application.properties
   AZURE_AD_ENABLED=true
   JWT_LOCAL_ENABLED=false
   ```

2. Reiniciar el backend

3. Acceder a `http://localhost:4200`

4. Hacer clic en "Iniciar sesión con Microsoft"

## 🐛 Troubleshooting

### El frontend no detecta el método de autenticación

**Problema**: Muestra "No configurado" o error al cargar configuración.

**Solución**:
1. Verificar que el backend esté corriendo en `http://localhost:8080`
2. Verificar CORS en el backend
3. Revisar la consola del navegador para errores de red

### Error 401 al iniciar sesión local

**Problema**: Credenciales rechazadas.

**Solución**:
1. Verificar que el usuario existe en la base de datos
2. Verificar que la contraseña esté hasheada correctamente con BCrypt
3. Revisar logs del backend

### Permisos no se cargan

**Problema**: Usuario autenticado pero sin permisos.

**Solución**:
1. Verificar que el usuario tenga perfiles asignados en la BD
2. Verificar que los perfiles tengan permisos asignados
3. Llamar manualmente a `authorizationService.initializeUserPermissions()`

## 📝 Notas Importantes

1. **Solo un método activo**: El sistema valida que al menos un método esté habilitado, pero se recomienda tener solo uno activo a la vez.

2. **Tokens separados**: Los tokens de Azure AD y JWT local son completamente independientes.

3. **Permisos compartidos**: Ambos métodos usan el mismo sistema de permisos del backend.

4. **Logout**: El logout limpia tanto los tokens como los permisos almacenados.

## 🎨 Personalización

### Modificar el Selector de Autenticación

Editar `src/app/components/auth-selector.component.html` y `.scss` para cambiar el diseño.

### Modificar el Formulario de Login

Editar `src/app/components/local-login.component.html` y `.scss` para cambiar el diseño del formulario.

### Agregar Validaciones

Modificar `local-login.component.ts` método `onSubmit()` para agregar validaciones adicionales.

## 📚 Referencias

- [MSAL Angular Documentation](https://github.com/AzureAD/microsoft-authentication-library-for-js)
- [JWT.io](https://jwt.io/) - Para decodificar y validar tokens JWT
- [BCrypt](https://www.npmjs.com/package/bcrypt) - Para hashear contraseñas

## ✅ Checklist de Implementación

- [x] Servicio de configuración de autenticación
- [x] Servicio de autenticación local con JWT
- [x] Interceptor HTTP para tokens JWT
- [x] Componente selector de autenticación
- [x] Componente de login local
- [x] Guard unificado de autenticación
- [x] Actualización de app.module
- [x] Actualización de app.component
- [x] Estilos CSS para nuevos componentes
- [x] Rutas actualizadas en app-routing
- [x] Sin errores de linting

## 🚀 Próximos Pasos Recomendados

1. **Testing**: Implementar tests unitarios y e2e para ambos flujos de autenticación
2. **Refresh Token**: Implementar renovación automática de tokens JWT
3. **Remember Me**: Agregar funcionalidad "Recordarme" en login local
4. **Multi-factor**: Agregar soporte para autenticación de dos factores
5. **Analytics**: Agregar métricas de uso de cada método de autenticación

---

**Versión**: 1.0.0  
**Fecha**: Octubre 2024  
**Estado**: ✅ Completado

