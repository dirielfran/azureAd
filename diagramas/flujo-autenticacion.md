# 🔐 Flujo de Autenticación - Angular + Spring Boot + Azure AD

## Diagrama del Flujo Completo

```mermaid
sequenceDiagram
    participant U as Usuario
    participant A as Angular App
    participant MS as Microsoft Entra ID
    participant S as Spring Boot API
    participant DB as Base de Datos

    Note over U,DB: 1. PROCESO DE LOGIN
    U->>A: Clic "Iniciar Sesión"
    A->>MS: Redirección a Azure AD
    MS->>U: Página de login
    U->>MS: Credenciales
    MS->>A: Token JWT + Código de autorización
    A->>A: Almacena tokens en localStorage

    Note over U,DB: 2. PROCESO DE AUTORIZACIÓN
    A->>S: GET /api/autorizacion/informacion-usuario
    Note right of A: Header: Authorization: Bearer <token>
    S->>S: Valida JWT token
    S->>S: Extrae grupos de Azure AD
    S->>DB: Busca perfiles por azure_group_id
    DB->>S: Retorna perfiles encontrados
    S->>DB: Obtiene permisos de cada perfil
    DB->>S: Retorna permisos
    S->>A: Información completa del usuario
    A->>A: Almacena permisos en SessionStorage
    A->>A: Actualiza UI según permisos

    Note over U,DB: 3. USO DE LA APLICACIÓN
    U->>A: Navega a página protegida
    A->>A: Verifica permisos con Guards
    A->>A: Renderiza contenido con Directivas
    U->>A: Hace clic en botón protegido
    A->>S: Llamada a API protegida
    Note right of A: Token agregado automáticamente
    S->>S: Valida token y permisos
    S->>A: Respuesta con datos
    A->>U: Muestra resultado
```

## Componentes del Flujo

### 🔑 **Autenticación (Login)**
1. **Usuario** hace clic en "Iniciar Sesión"
2. **Angular** redirige a Microsoft Entra ID
3. **Microsoft** autentica al usuario
4. **Microsoft** retorna token JWT con grupos
5. **Angular** almacena tokens automáticamente

### 🛡️ **Autorización (Permisos)**
1. **Angular** solicita información del usuario
2. **Spring Boot** valida token JWT
3. **Spring Boot** extrae grupos de Azure AD
4. **Base de Datos** busca perfiles por grupo
5. **Base de Datos** retorna permisos del perfil
6. **Angular** almacena permisos en SessionStorage

### 🎯 **Uso de la Aplicación**
1. **Guards** protegen rutas según permisos
2. **Directivas** renderizan contenido condicionalmente
3. **Interceptor** agrega tokens automáticamente
4. **APIs** validan permisos en cada request
