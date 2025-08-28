-- =============================================================================
-- DATOS DE EJEMPLO PARA EL SISTEMA DE PERMISOS Y ROLES
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. PERMISOS DEL SISTEMA
-- -----------------------------------------------------------------------------
-- Permisos para el módulo USUARIOS
INSERT INTO permisos (codigo, nombre, descripcion, modulo, accion, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('USUARIOS_LEER', 'Ver Usuarios', 'Permite ver la lista de usuarios', 'USUARIOS', 'LEER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, accion, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('USUARIOS_CREAR', 'Crear Usuarios', 'Permite crear nuevos usuarios', 'USUARIOS', 'CREAR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, accion, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('USUARIOS_EDITAR', 'Editar Usuarios', 'Permite modificar usuarios existentes', 'USUARIOS', 'EDITAR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, accion, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('USUARIOS_ELIMINAR', 'Eliminar Usuarios', 'Permite eliminar usuarios', 'USUARIOS', 'ELIMINAR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Permisos para el módulo REPORTES
INSERT INTO permisos (codigo, nombre, descripcion, modulo, accion, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('REPORTES_LEER', 'Ver Reportes', 'Permite ver reportes del sistema', 'REPORTES', 'LEER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, accion, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('REPORTES_CREAR', 'Crear Reportes', 'Permite crear nuevos reportes', 'REPORTES', 'CREAR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, accion, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('REPORTES_EXPORTAR', 'Exportar Reportes', 'Permite exportar reportes', 'REPORTES', 'EXPORTAR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Permisos para el módulo CONFIGURACION
INSERT INTO permisos (codigo, nombre, descripcion, modulo, accion, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('CONFIG_LEER', 'Ver Configuración', 'Permite ver configuración del sistema', 'CONFIGURACION', 'LEER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, accion, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('CONFIG_EDITAR', 'Editar Configuración', 'Permite modificar configuración del sistema', 'CONFIGURACION', 'EDITAR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Permisos para el módulo PERFILES
INSERT INTO permisos (codigo, nombre, descripcion, modulo, accion, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('PERFILES_LEER', 'Ver Perfiles', 'Permite ver perfiles y roles', 'PERFILES', 'LEER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, accion, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('PERFILES_CREAR', 'Crear Perfiles', 'Permite crear nuevos perfiles', 'PERFILES', 'CREAR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, accion, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('PERFILES_EDITAR', 'Editar Perfiles', 'Permite modificar perfiles existentes', 'PERFILES', 'EDITAR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, accion, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('PERFILES_ELIMINAR', 'Eliminar Perfiles', 'Permite eliminar perfiles', 'PERFILES', 'ELIMINAR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Permisos para el módulo DASHBOARD
INSERT INTO permisos (codigo, nombre, descripcion, modulo, accion, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('DASHBOARD_LEER', 'Ver Dashboard', 'Permite acceso al dashboard principal', 'DASHBOARD', 'LEER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, accion, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('DASHBOARD_ADMIN', 'Dashboard Administrador', 'Acceso completo al dashboard de administrador', 'DASHBOARD', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- -----------------------------------------------------------------------------
-- 2. PERFILES DEL SISTEMA (asociados a grupos de Azure AD)
-- -----------------------------------------------------------------------------
-- PERFIL ADMINISTRADOR
INSERT INTO perfiles (nombre, descripcion, azure_group_id, azure_group_name, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('Administrador', 'Acceso completo al sistema', 'bdff3193-e802-41d9-a5c6-edc6fb0db732', 'Administradores Azure', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- PERFIL GESTOR
INSERT INTO perfiles (nombre, descripcion, azure_group_id, azure_group_name, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('Gestor', 'Acceso de gestión con permisos limitados', 'manager-group-id-456', 'Gestores', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- PERFIL USUARIO ESTANDAR
INSERT INTO perfiles (nombre, descripcion, azure_group_id, azure_group_name, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('Usuario', 'Usuario estándar con permisos básicos', '1ae2b90c-5c46-4639-84d0-809d66cdd809', 'Usuarios Azure', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- PERFIL SOLO LECTURA
INSERT INTO perfiles (nombre, descripcion, azure_group_id, azure_group_name, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('Lector', 'Solo permisos de lectura', 'reader-group-id-101', 'Lectores', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Perfil por defecto para usuarios sin grupos específicos
INSERT INTO perfiles (nombre, descripcion, azure_group_id, azure_group_name, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('Usuario Básico', 'Perfil por defecto para usuarios autenticados', 'default-user', 'Usuarios Básicos', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- -----------------------------------------------------------------------------
-- 3. ASIGNACIÓN DE PERMISOS A PERFILES (Tabla de relación many-to-many)
-- -----------------------------------------------------------------------------
-- PERMISOS PARA ADMINISTRADOR (ID = 1) - Todos los permisos
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (1, 1);  -- USUARIOS_LEER
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (1, 2);  -- USUARIOS_CREAR
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (1, 3);  -- USUARIOS_EDITAR
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (1, 4);  -- USUARIOS_ELIMINAR
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (1, 5);  -- REPORTES_LEER
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (1, 6);  -- REPORTES_CREAR
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (1, 7);  -- REPORTES_EXPORTAR
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (1, 8);  -- CONFIG_LEER
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (1, 9);  -- CONFIG_EDITAR
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (1, 10); -- PERFILES_LEER
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (1, 11); -- PERFILES_CREAR
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (1, 12); -- PERFILES_EDITAR
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (1, 13); -- PERFILES_ELIMINAR
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (1, 14); -- DASHBOARD_LEER
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (1, 15); -- DASHBOARD_ADMIN

-- PERMISOS PARA GESTOR (ID = 2) - Permisos de gestión
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (2, 1);  -- USUARIOS_LEER
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (2, 2);  -- USUARIOS_CREAR
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (2, 3);  -- USUARIOS_EDITAR
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (2, 5);  -- REPORTES_LEER
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (2, 6);  -- REPORTES_CREAR
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (2, 7);  -- REPORTES_EXPORTAR
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (2, 8);  -- CONFIG_LEER
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (2, 10); -- PERFILES_LEER
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (2, 14); -- DASHBOARD_LEER

-- PERMISOS PARA USUARIO (ID = 3) - Permisos básicos
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (3, 1);  -- USUARIOS_LEER
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (3, 5);  -- REPORTES_LEER
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (3, 8);  -- CONFIG_LEER
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (3, 14); -- DASHBOARD_LEER

-- PERMISOS PARA LECTOR (ID = 4) - Solo lectura
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (4, 1);  -- USUARIOS_LEER
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (4, 5);  -- REPORTES_LEER
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (4, 8);  -- CONFIG_LEER
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (4, 14); -- DASHBOARD_LEER

-- PERMISOS PARA USUARIO BÁSICO (ID = 5) - Perfil por defecto
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (5, 1);  -- USUARIOS_LEER
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (5, 5);  -- REPORTES_LEER
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (5, 8);  -- CONFIG_LEER
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (5, 10); -- PERFILES_LEER
INSERT INTO perfil_permisos (perfil_id, permiso_id) VALUES (5, 14); -- DASHBOARD_LEER

-- -----------------------------------------------------------------------------
-- 4. DATOS DE EJEMPLO PARA LA TABLA USUARIOS
-- -----------------------------------------------------------------------------
INSERT INTO usuarios (nombre, email, azure_object_id, departamento, cargo, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('Juan Pérez', 'juan.perez@empresa.com', 'azure-obj-id-1', 'Desarrollo', 'Desarrollador Senior', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO usuarios (nombre, email, azure_object_id, departamento, cargo, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('María García', 'maria.garcia@empresa.com', 'azure-obj-id-2', 'Diseño', 'Diseñadora UX', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO usuarios (nombre, email, azure_object_id, departamento, cargo, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('Carlos López', 'carlos.lopez@empresa.com', 'azure-obj-id-3', 'Marketing', 'Especialista en Marketing', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO usuarios (nombre, email, azure_object_id, departamento, cargo, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('Ana Rodríguez', 'ana.rodriguez@empresa.com', 'azure-obj-id-4', 'Desarrollo', 'Desarrolladora Frontend', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO usuarios (nombre, email, azure_object_id, departamento, cargo, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('Luis Martínez', 'luis.martinez@empresa.com', 'azure-obj-id-5', 'Operaciones', 'Gerente de Operaciones', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO usuarios (nombre, email, azure_object_id, departamento, cargo, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('Sofia Hernández', 'sofia.hernandez@empresa.com', 'azure-obj-id-6', 'Recursos Humanos', 'Coordinadora de RRHH', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO usuarios (nombre, email, azure_object_id, departamento, cargo, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('Diego Torres', 'diego.torres@empresa.com', 'azure-obj-id-7', 'Desarrollo', 'Arquitecto de Software', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO usuarios (nombre, email, azure_object_id, departamento, cargo, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('Lucía Morales', 'lucia.morales@empresa.com', 'azure-obj-id-8', 'Finanzas', 'Analista Financiero', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO usuarios (nombre, email, azure_object_id, departamento, cargo, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('Roberto Silva', 'roberto.silva@empresa.com', 'azure-obj-id-9', 'Ventas', 'Ejecutivo de Ventas', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO usuarios (nombre, email, azure_object_id, departamento, cargo, activo, fecha_creacion, fecha_actualizacion) 
VALUES ('Carmen Jiménez', 'carmen.jimenez@empresa.com', 'azure-obj-id-10', 'Marketing', 'Coordinadora de Marketing', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
