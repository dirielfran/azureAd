-- ================================================================
-- 🔧 ASIGNAR PERFILES A USUARIOS LOCALES
-- ================================================================
-- Este script asigna perfiles a los usuarios locales para que
-- puedan cargar sus permisos correctamente

-- Verificar usuarios sin perfiles
SELECT 
    u.id,
    u.email, 
    u.nombre,
    COUNT(up.perfil_id) as num_perfiles
FROM usuarios u
LEFT JOIN usuario_perfil up ON u.id = up.usuario_id
WHERE u.email LIKE '%@local.com'
GROUP BY u.id, u.email, u.nombre;

-- Asignar perfil "Usuario Básico" (ID: 1) a admin@local.com
INSERT INTO usuario_perfil (usuario_id, perfil_id)
SELECT u.id, 1
FROM usuarios u
WHERE u.email = 'admin@local.com'
AND NOT EXISTS (
    SELECT 1 FROM usuario_perfil up 
    WHERE up.usuario_id = u.id AND up.perfil_id = 1
);

-- Asignar perfil "Usuario Básico" (ID: 1) a user@local.com
INSERT INTO usuario_perfil (usuario_id, perfil_id)
SELECT u.id, 1
FROM usuarios u
WHERE u.email = 'user@local.com'
AND NOT EXISTS (
    SELECT 1 FROM usuario_perfil up 
    WHERE up.usuario_id = u.id AND up.perfil_id = 1
);

-- Asignar perfil "Usuario Básico" (ID: 1) a guest@local.com
INSERT INTO usuario_perfil (usuario_id, perfil_id)
SELECT u.id, 1
FROM usuarios u
WHERE u.email = 'guest@local.com'
AND NOT EXISTS (
    SELECT 1 FROM usuario_perfil up 
    WHERE up.usuario_id = u.id AND up.perfil_id = 1
);

-- Verificar que se asignaron correctamente
SELECT 
    u.email,
    u.nombre,
    p.nombre as perfil,
    COUNT(perm.id) as num_permisos
FROM usuarios u
INNER JOIN usuario_perfil up ON u.id = up.usuario_id
INNER JOIN perfil p ON up.perfil_id = p.id
LEFT JOIN perfil_permiso pp ON p.id = pp.perfil_id
LEFT JOIN permisos perm ON pp.permiso_id = perm.id AND perm.activo = true
WHERE u.email LIKE '%@local.com'
GROUP BY u.email, u.nombre, p.nombre;

