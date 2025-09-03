# 🔐 Sistema de Permisos y Roles

## Diagrama del Sistema de Permisos

```mermaid
graph TD
    subgraph "👥 Grupos de Azure AD"
        A1[Administradores<br/>bdff3193-e802-41d9-a5c6-edc6fb0db732]
        A2[Usuarios<br/>1ae2b90c-5c46-4639-84d0-809d66cdd809]
        A3[Gestores<br/>manager-group-id-456]
        A4[Lectores<br/>reader-group-id-101]
    end

    subgraph "🎭 Perfiles del Sistema"
        B1[Administrador<br/>Acceso completo]
        B2[Usuario<br/>Permisos básicos]
        B3[Gestor<br/>Permisos de gestión]
        B4[Lector<br/>Solo lectura]
        B5[Usuario Básico<br/>Perfil por defecto]
    end

    subgraph "🔑 Permisos por Módulo"
        C1[USUARIOS<br/>LEER, CREAR, EDITAR, ELIMINAR]
        C2[REPORTES<br/>LEER, CREAR, EXPORTAR]
        C3[CONFIGURACION<br/>LEER, EDITAR]
        C4[PERFILES<br/>LEER, CREAR, EDITAR, ELIMINAR]
        C5[DASHBOARD<br/>LEER, ADMIN]
    end

    subgraph "🎯 Asignaciones"
        D1[Administrador → Todos los permisos]
        D2[Gestor → Gestión usuarios + reportes]
        D3[Usuario → Permisos básicos]
        D4[Lector → Solo lectura]
        D5[Usuario Básico → Permisos mínimos]
    end

    %% Conexiones Grupos → Perfiles
    A1 --> B1
    A2 --> B2
    A3 --> B3
    A4 --> B4

    %% Conexiones Perfiles → Permisos
    B1 --> C1
    B1 --> C2
    B1 --> C3
    B1 --> C4
    B1 --> C5

    B2 --> C1
    B2 --> C2
    B2 --> C3
    B2 --> C5

    B3 --> C1
    B3 --> C2
    B3 --> C3
    B3 --> C5

    B4 --> C1
    B4 --> C2
    B4 --> C3
    B4 --> C5

    B5 --> C1
    B5 --> C2
    B5 --> C3
    B5 --> C4
    B5 --> C5

    %% Estilos
    classDef azure fill:#f57c00,stroke:#e65100,color:#ffffff
    classDef profile fill:#1976d2,stroke:#0d47a1,color:#ffffff
    classDef permission fill:#7b1fa2,stroke:#4a148c,color:#ffffff
    classDef assignment fill:#388e3c,stroke:#1b5e20,color:#ffffff

    class A1,A2,A3,A4 azure
    class B1,B2,B3,B4,B5 profile
    class C1,C2,C3,C4,C5 permission
    class D1,D2,D3,D4,D5 assignment
```

## Detalle de Permisos por Perfil

### 🔴 **Administrador** (Todos los permisos)
```mermaid
pie title Permisos del Administrador
    "USUARIOS" : 4
    "REPORTES" : 3
    "CONFIGURACION" : 2
    "PERFILES" : 4
    "DASHBOARD" : 2
```

### 🟡 **Gestor** (Permisos de gestión)
```mermaid
pie title Permisos del Gestor
    "USUARIOS" : 3
    "REPORTES" : 3
    "CONFIGURACION" : 1
    "DASHBOARD" : 1
```

### 🟢 **Usuario** (Permisos básicos)
```mermaid
pie title Permisos del Usuario
    "USUARIOS" : 1
    "REPORTES" : 1
    "CONFIGURACION" : 1
    "DASHBOARD" : 1
```

### 🔵 **Lector** (Solo lectura)
```mermaid
pie title Permisos del Lector
    "USUARIOS" : 1
    "REPORTES" : 1
    "CONFIGURACION" : 1
    "DASHBOARD" : 1
```

## Flujo de Validación de Permisos

```mermaid
flowchart TD
    A[Usuario hace acción] --> B{¿Está autenticado?}
    B -->|No| C[Redirigir a login]
    B -->|Sí| D[Obtener token JWT]
    D --> E[Extraer grupos de Azure AD]
    E --> F[Buscar perfiles por grupo]
    F --> G{¿Perfil encontrado?}
    G -->|No| H[Asignar perfil por defecto]
    G -->|Sí| I[Obtener permisos del perfil]
    H --> I
    I --> J{¿Tiene el permiso?}
    J -->|No| K[Mostrar acceso denegado]
    J -->|Sí| L[Permitir acción]
    
    %% Estilos
    classDef success fill:#388e3c,stroke:#1b5e20,color:#ffffff
    classDef error fill:#d32f2f,stroke:#b71c1c,color:#ffffff
    classDef process fill:#1976d2,stroke:#0d47a1,color:#ffffff
    
    class L success
    class C,K error
    class A,B,D,E,F,G,H,I,J process
```

## Códigos de Permisos Disponibles

| Módulo | Código | Descripción |
|--------|--------|-------------|
| **USUARIOS** | `USUARIOS_LEER` | Ver lista de usuarios |
| | `USUARIOS_CREAR` | Crear nuevos usuarios |
| | `USUARIOS_EDITAR` | Modificar usuarios |
| | `USUARIOS_ELIMINAR` | Eliminar usuarios |
| **REPORTES** | `REPORTES_LEER` | Ver reportes |
| | `REPORTES_CREAR` | Crear reportes |
| | `REPORTES_EXPORTAR` | Exportar reportes |
| **CONFIGURACION** | `CONFIG_LEER` | Ver configuración |
| | `CONFIG_EDITAR` | Editar configuración |
| **PERFILES** | `PERFILES_LEER` | Ver perfiles |
| | `PERFILES_CREAR` | Crear perfiles |
| | `PERFILES_EDITAR` | Editar perfiles |
| | `PERFILES_ELIMINAR` | Eliminar perfiles |
| **DASHBOARD** | `DASHBOARD_LEER` | Acceso al dashboard |
| | `DASHBOARD_ADMIN` | Dashboard de administrador |
