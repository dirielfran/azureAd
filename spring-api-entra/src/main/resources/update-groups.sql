-- Actualizar los IDs de grupos de Azure AD con los valores reales
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
