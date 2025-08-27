# Configuración de API Protegida con Microsoft Entra ID

## 🚀 Estado Actual
Tu aplicación Angular ya está configurada para trabajar con APIs protegidas usando Microsoft Entra ID (Azure AD). Los componentes principales están implementados y funcionando.

## 📋 Qué se ha configurado

### 1. Servicio de API (`src/app/services/api.service.ts`)
- ✅ Servicio completo para manejar llamadas HTTP a APIs protegidas
- ✅ Métodos para GET, POST, PUT, DELETE
- ✅ Integración con Microsoft Graph API
- ✅ Manejo de errores y autenticación
- ✅ Obtención de tokens de acceso

### 2. Componente de demostración (`src/app/components/protected-data.component.ts`)
- ✅ Interfaz completa para probar APIs protegidas
- ✅ Botones para obtener perfil de usuario y datos protegidos
- ✅ Visualización de tokens de acceso
- ✅ Manejo de estados de carga y errores

### 3. Configuración MSAL (`src/app/app.module.ts`)
- ✅ Interceptor configurado para agregar tokens automáticamente
- ✅ Guards para proteger rutas
- ✅ Configuración de scopes y recursos protegidos

## 🔧 Personalizar para tu API

### Paso 1: Configurar tu API en Azure AD

1. **Ve al Portal de Azure** (https://portal.azure.com)
2. **Navega a "Azure Active Directory" > "App registrations"**
3. **Selecciona tu aplicación** o crea una nueva
4. **En "Expose an API":**
   - Agrega un Application ID URI (ej: `api://tu-app-id`)
   - Agrega scopes (ej: `access_as_user`, `read`, `write`)
5. **En "API permissions":**
   - Agrega los permisos necesarios para tu API

### Paso 2: Actualizar la configuración en tu código

Edita el archivo `src/app/config/api.config.ts`:

```typescript
export const API_CONFIG: ApiConfig = {
  // 🔄 CAMBIA ESTA URL por la de tu API real
  baseUrl: 'https://tu-api-real.com/api',
  
  // 🔄 CAMBIA ESTOS SCOPES por los de tu API
  scopes: [
    'api://tu-app-id-real/access_as_user',
    // Agrega más scopes si necesitas
  ],
  
  // 🔄 PERSONALIZA los endpoints según tu API
  endpoints: {
    users: '/users',
    profile: '/profile',
    data: '/data',
    // Agrega los endpoints que necesites
  }
};
```

### Paso 3: Actualizar la configuración MSAL

En `src/app/app.module.ts`, actualiza las URLs y scopes:

```typescript
// Línea ~31: Cambia la URL de tu API
protectedResourceMap.set('https://tu-api-real.com/api', ['api://tu-app-id-real/access_as_user']);

// Línea ~21: Agrega los scopes necesarios
scopes: [
  'user.read',
  'api://tu-app-id-real/access_as_user'
]
```

### Paso 4: Configurar el entorno

En `src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  msalConfig: {
    auth: {
      clientId: 'tu-client-id-real', // 🔄 CAMBIA por tu Client ID real
      authority: 'https://login.microsoftonline.com/tu-tenant-id', // 🔄 CAMBIA por tu Tenant ID
      redirectUri: 'http://localhost:4200',
    },
    // ... resto de la configuración
  }
};
```

## 🧪 Probar la configuración

1. **Inicia la aplicación:**
   ```bash
   npm start
   ```

2. **Navega a** `http://localhost:4200`

3. **Inicia sesión** con tu cuenta de Microsoft

4. **Prueba los botones:**
   - "Obtener Perfil de Usuario" - debe funcionar con Microsoft Graph
   - "Obtener Datos Protegidos" - probará tu API personalizada
   - "Ver Token de Acceso" - muestra el token JWT

## 🔍 Debugging

### Ver tokens en la consola
Los tokens se imprimen en la consola del navegador para debugging.

### Verificar la configuración
1. Abre las herramientas de desarrollador (F12)
2. Ve a la pestaña "Network"
3. Realiza una llamada a la API
4. Verifica que el header `Authorization: Bearer <token>` esté presente

### Errores comunes

**Error 401 (No autorizado):**
- Verifica que los scopes estén correctos
- Confirma que la URL de la API esté en el `protectedResourceMap`

**Error 403 (Acceso denegado):**
- Verifica los permisos en Azure AD
- Confirma que el usuario tenga acceso a la API

**Error de CORS:**
- Configura CORS en tu API para permitir `http://localhost:4200`
- En producción, agrega tu dominio real

## 📚 Recursos adicionales

- [Documentación de MSAL Angular](https://github.com/AzureAD/microsoft-authentication-library-for-js/tree/dev/lib/msal-angular)
- [Microsoft Graph API](https://docs.microsoft.com/en-us/graph/)
- [Azure AD App Registration](https://docs.microsoft.com/en-us/azure/active-directory/develop/quickstart-register-app)

## 🎯 Próximos pasos

1. **Personaliza la configuración** con los valores reales de tu API
2. **Prueba la integración** con tu API backend
3. **Implementa más funcionalidades** según tus necesidades
4. **Despliega a producción** actualizando `environment.prod.ts`

¡Tu aplicación ya está lista para trabajar con APIs protegidas! 🎉
