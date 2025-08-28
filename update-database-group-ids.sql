-- =============================================================================
-- ACTUALIZACIÓN DE IDS DE GRUPOS DE AZURE AD EN LA BASE DE DATOS
-- =============================================================================
-- Este script actualiza los azure_group_id con los IDs reales de Azure AD
-- 
-- IDs proporcionados:
-- - bdff3193-e802-41d9-a5c6-edc6fb0db732
-- - 1ae2b90c-5c46-4639-84d0-809d66cdd809
-- =============================================================================

-- Ver el estado actual de los perfiles antes de la actualización
SELECT 'ESTADO ACTUAL:' as info;
SELECT id, nombre, descripcion, azure_group_id, azure_group_name, activo 
FROM perfiles 
ORDER BY id;

-- Actualizar los perfiles con los IDs reales de Azure AD
-- Asumiendo que el primer ID es para Administradores y el segundo para otro grupo

-- OPCIÓN 1: Si tienes 2 grupos (Administradores y Usuarios)
UPDATE perfiles 
SET azure_group_id = 'bdff3193-e802-41d9-a5c6-edc6fb0db732',
    azure_group_name = 'Administradores Azure',
    fecha_actualizacion = CURRENT_TIMESTAMP
WHERE nombre = 'Administrador';

UPDATE perfiles 
SET azure_group_id = '1ae2b90c-5c46-4639-84d0-809d66cdd809',
    azure_group_name = 'Usuarios Azure',
    fecha_actualizacion = CURRENT_TIMESTAMP
WHERE nombre = 'Usuario';

-- OPCIÓN 2: Si el segundo grupo es para Gestores, descomenta esta línea:
-- UPDATE perfiles 
-- SET azure_group_id = '1ae2b90c-5c46-4639-84d0-809d66cdd809',
--     azure_group_name = 'Gestores Azure',
--     fecha_actualizacion = CURRENT_TIMESTAMP
-- WHERE nombre = 'Gestor';

-- Mantener el perfil de Lector con un ID genérico para usuarios sin grupos específicos
UPDATE perfiles 
SET azure_group_id = 'default-reader-group',
    azure_group_name = 'Lectores por Defecto',
    fecha_actualizacion = CURRENT_TIMESTAMP
WHERE nombre = 'Lector';

-- Mantener el perfil básico como fallback
UPDATE perfiles 
SET azure_group_id = 'default-basic-user',
    azure_group_name = 'Usuarios Básicos por Defecto',
    fecha_actualizacion = CURRENT_TIMESTAMP
WHERE nombre = 'Usuario Básico';

-- Ver el estado después de la actualización
SELECT 'ESTADO DESPUÉS DE LA ACTUALIZACIÓN:' as info;
SELECT id, nombre, descripcion, azure_group_id, azure_group_name, activo 
FROM perfiles 
ORDER BY id;

-- Verificar que los IDs se actualizaron correctamente
SELECT 'VERIFICACIÓN DE ACTUALIZACIÓN:' as info;
SELECT 
    nombre,
    azure_group_id,
    CASE 
        WHEN azure_group_id = 'bdff3193-e802-41d9-a5c6-edc6fb0db732' THEN '✓ ID Actualizado Correctamente'
        WHEN azure_group_id = '1ae2b90c-5c46-4639-84d0-809d66cdd809' THEN '✓ ID Actualizado Correctamente'
        WHEN azure_group_id LIKE 'default-%' THEN '⚠ ID por Defecto (OK)'
        ELSE '❌ ID No Actualizado'
    END as estado
FROM perfiles
ORDER BY id;
