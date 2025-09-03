# 📊 Diagramas del Sistema Angular-Entra-Auth

Este directorio contiene todos los diagramas visuales que explican el funcionamiento del sistema de autenticación y autorización.

## 📋 Lista de Diagramas

### 1. 🔐 [Flujo de Autenticación](flujo-autenticacion.md)
- **Descripción**: Secuencia completa del proceso de login y autorización
- **Incluye**: 
  - Proceso de login con Microsoft Entra ID
  - Flujo de autorización y obtención de permisos
  - Uso de la aplicación con permisos

### 2. 🏗️ [Arquitectura del Sistema](arquitectura-sistema.md)
- **Descripción**: Vista general de la arquitectura del sistema
- **Incluye**:
  - Componentes del frontend (Angular)
  - Servicios de Microsoft Entra ID
  - Backend Spring Boot
  - Base de datos H2

### 3. 🔐 [Sistema de Permisos](sistema-permisos.md)
- **Descripción**: Estructura y funcionamiento del sistema de permisos
- **Incluye**:
  - Grupos de Azure AD
  - Perfiles del sistema
  - Permisos por módulo
  - Asignaciones de permisos

### 4. 🎨 [Componentes del Frontend](componentes-frontend.md)
- **Descripción**: Estructura de componentes, servicios y directivas de Angular
- **Incluye**:
  - Componentes principales
  - Guards de rutas
  - Directivas personalizadas
  - Servicios de autorización

### 5. 🚀 [Endpoints de la API](endpoints-api.md)
- **Descripción**: Documentación completa de todos los endpoints
- **Incluye**:
  - Endpoints públicos y protegidos
  - Controladores y sus métodos
  - Ejemplos de respuestas
  - Códigos de estado HTTP

### 6. 💾 [Base de Datos](base-datos.md)
- **Descripción**: Modelo de datos y estructura de la base de datos
- **Incluye**:
  - Diagrama entidad-relación
  - Estructura de tablas
  - Datos de ejemplo
  - Scripts de consulta

## 🛠️ Cómo Visualizar los Diagramas

### **Opción 1: GitHub/GitLab**
Los diagramas están escritos en formato Mermaid y se renderizan automáticamente en:
- GitHub
- GitLab
- Bitbucket

### **Opción 2: Editores Online**
- [Mermaid Live Editor](https://mermaid.live/)
- [Mermaid Chart](https://www.mermaidchart.com/)

### **Opción 3: Extensiones de VS Code**
- [Mermaid Preview](https://marketplace.visualstudio.com/items?itemName=vstirbu.vscode-mermaid-preview)
- [Markdown Preview Mermaid Support](https://marketplace.visualstudio.com/items?itemName=bierner.markdown-mermaid)

### **Opción 4: Herramientas de Documentación**
- [GitBook](https://www.gitbook.com/)
- [Notion](https://www.notion.so/)
- [Confluence](https://www.atlassian.com/software/confluence)

## 📖 Cómo Usar los Diagramas

### **Para Desarrolladores:**
1. **Entender la arquitectura**: Comienza con `arquitectura-sistema.md`
2. **Comprender el flujo**: Revisa `flujo-autenticacion.md`
3. **Implementar permisos**: Estudia `sistema-permisos.md`
4. **Desarrollar frontend**: Consulta `componentes-frontend.md`
5. **Crear APIs**: Usa `endpoints-api.md` como referencia
6. **Configurar BD**: Sigue `base-datos.md`

### **Para Administradores:**
1. **Configurar Azure AD**: Revisa `sistema-permisos.md`
2. **Gestionar usuarios**: Consulta `base-datos.md`
3. **Monitorear sistema**: Usa `endpoints-api.md`

### **Para Arquitectos:**
1. **Vista general**: `arquitectura-sistema.md`
2. **Flujo de datos**: `flujo-autenticacion.md`
3. **Modelo de datos**: `base-datos.md`

## 🔄 Actualización de Diagramas

Los diagramas se actualizan cuando:
- Se agregan nuevos endpoints
- Se modifican permisos o perfiles
- Se cambia la arquitectura
- Se actualiza la base de datos

### **Para Actualizar un Diagrama:**
1. Edita el archivo `.md` correspondiente
2. Modifica el código Mermaid
3. Verifica que se renderice correctamente
4. Actualiza la documentación si es necesario

## 📚 Recursos Adicionales

### **Documentación Mermaid:**
- [Sintaxis de Mermaid](https://mermaid-js.github.io/mermaid/)
- [Tipos de Diagramas](https://mermaid-js.github.io/mermaid/#/diagram)
- [Ejemplos](https://mermaid-js.github.io/mermaid/#/examples)

### **Herramientas de Diseño:**
- [Draw.io](https://app.diagrams.net/) - Para diagramas más complejos
- [Lucidchart](https://www.lucidchart.com/) - Herramienta profesional
- [Figma](https://www.figma.com/) - Para diseños de UI/UX

## 🎯 Próximos Diagramas Sugeridos

1. **Diagrama de Despliegue**: Mostrar cómo se despliega el sistema
2. **Diagrama de Secuencia Detallado**: Flujo específico de cada endpoint
3. **Diagrama de Estados**: Estados de autenticación y autorización
4. **Diagrama de Componentes**: Estructura detallada de cada componente
5. **Diagrama de Base de Datos Físico**: Índices, particiones, etc.

---

**¡Los diagramas están listos para usar y ayudar a entender el sistema!** 🚀
