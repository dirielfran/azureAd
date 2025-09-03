# 🏗️ Arquitectura del Sistema - Angular + Spring Boot + Azure AD

## Diagrama de Arquitectura General

```mermaid
graph TB
    subgraph "🌐 Frontend - Angular"
        A[AppComponent<br/>Navegación Principal]
        B[ProtectedDataComponent<br/>Demostración APIs]
        C[UserPermissionsComponent<br/>Dashboard Permisos]
        D[AuthorizationService<br/>Gestión Permisos]
        E[MSAL Interceptor<br/>Tokens Automáticos]
        F[Guards & Directives<br/>Protección UI]
    end

    subgraph "☁️ Microsoft Entra ID"
        G[Azure AD<br/>Autenticación]
        H[Grupos de Seguridad<br/>Administradores, Usuarios, etc.]
        I[JWT Tokens<br/>Con grupos incluidos]
    end

    subgraph "🚀 Backend - Spring Boot"
        J[AuthController<br/>Autenticación]
        K[AuthorizationController<br/>Gestión Permisos]
        L[DataController<br/>Datos Protegidos]
        M[SecurityConfig<br/>Configuración Seguridad]
        N[AzureAdGroupsJwtConverter<br/>Conversión Grupos]
    end

    subgraph "💾 Base de Datos - H2"
        O[Tabla: usuarios<br/>Datos de usuarios]
        P[Tabla: perfiles<br/>Roles del sistema]
        Q[Tabla: permisos<br/>Permisos granulares]
        R[Tabla: perfil_permisos<br/>Relación many-to-many]
    end

    %% Conexiones Frontend
    A --> D
    B --> D
    C --> D
    D --> E
    E --> F

    %% Conexiones Autenticación
    A --> G
    G --> H
    H --> I
    I --> E

    %% Conexiones Backend
    E --> J
    E --> K
    E --> L
    J --> M
    K --> M
    L --> M
    M --> N

    %% Conexiones Base de Datos
    K --> O
    K --> P
    K --> Q
    K --> R
    N --> P

    %% Estilos
    classDef frontend fill:#1976d2,stroke:#0d47a1,color:#ffffff
    classDef azure fill:#f57c00,stroke:#e65100,color:#ffffff
    classDef backend fill:#7b1fa2,stroke:#4a148c,color:#ffffff
    classDef database fill:#388e3c,stroke:#1b5e20,color:#ffffff

    class A,B,C,D,E,F frontend
    class G,H,I azure
    class J,K,L,M,N backend
    class O,P,Q,R database
```

## Componentes por Capa

### 🌐 **Frontend (Angular)**
- **AppComponent**: Navegación principal y estado de autenticación
- **ProtectedDataComponent**: Demostración de APIs protegidas
- **UserPermissionsComponent**: Dashboard de permisos del usuario
- **AuthorizationService**: Gestión centralizada de permisos
- **MSAL Interceptor**: Agrega tokens automáticamente a peticiones HTTP
- **Guards & Directives**: Protección de rutas y renderizado condicional

### ☁️ **Microsoft Entra ID**
- **Azure AD**: Servicio de autenticación de Microsoft
- **Grupos de Seguridad**: Administradores, Usuarios, Gestores, etc.
- **JWT Tokens**: Contienen información del usuario y grupos

### 🚀 **Backend (Spring Boot)**
- **AuthController**: Endpoints de autenticación
- **AuthorizationController**: Gestión de permisos y perfiles
- **DataController**: Endpoints de datos protegidos
- **SecurityConfig**: Configuración de seguridad
- **AzureAdGroupsJwtConverter**: Convierte grupos de Azure AD a roles

### 💾 **Base de Datos (H2)**
- **usuarios**: Información de usuarios del sistema
- **perfiles**: Roles/perfiles asociados a grupos de Azure AD
- **permisos**: Permisos granulares del sistema
- **perfil_permisos**: Relación many-to-many entre perfiles y permisos

## Flujo de Datos

1. **Usuario** se autentica en Azure AD
2. **Azure AD** retorna JWT con grupos
3. **Angular** envía token a Spring Boot
4. **Spring Boot** valida token y extrae grupos
5. **Base de Datos** busca perfiles por grupos
6. **Base de Datos** retorna permisos asociados
7. **Angular** recibe permisos y actualiza UI
