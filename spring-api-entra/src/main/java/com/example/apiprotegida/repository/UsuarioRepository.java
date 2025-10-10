package com.example.apiprotegida.repository;

import com.example.apiprotegida.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Usuario
 * 
 * Proporciona operaciones CRUD y consultas personalizadas
 * para la gestión de usuarios en la API protegida.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    /**
     * Busca un usuario por su email
     * @param email El email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByEmail(String email);
    
    /**
     * Busca un usuario por su email cargando sus perfiles y permisos (para JWT)
     * @param email El email del usuario
     * @return Optional con el usuario y sus perfiles/permisos cargados
     */
    @Query("SELECT DISTINCT u FROM Usuario u " +
           "LEFT JOIN FETCH u.perfiles p " +
           "LEFT JOIN FETCH p.permisos " +
           "WHERE u.email = :email")
    Optional<Usuario> findByEmailWithPerfiles(@Param("email") String email);
    
    /**
     * Busca un usuario por su Azure Object ID
     * @param azureObjectId El Object ID de Azure AD
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByAzureObjectId(String azureObjectId);
    
    /**
     * Busca usuarios por departamento
     * @param departamento El departamento a buscar
     * @return Lista de usuarios del departamento
     */
    List<Usuario> findByDepartamento(String departamento);
    
    /**
     * Busca usuarios por estado activo
     * @param activo True para usuarios activos, false para inactivos
     * @return Lista de usuarios según el estado
     */
    List<Usuario> findByActivo(Boolean activo);
    
    /**
     * Busca usuarios por nombre (ignorando mayúsculas/minúsculas)
     * @param nombre El nombre a buscar
     * @return Lista de usuarios que contengan el nombre
     */
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Usuario> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);
    
    /**
     * Cuenta usuarios activos por departamento
     * @param departamento El departamento
     * @return Número de usuarios activos en el departamento
     */
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.departamento = :departamento AND u.activo = true")
    Long countActiveUsersByDepartamento(@Param("departamento") String departamento);
    
    /**
     * Obtiene todos los departamentos únicos
     * @return Lista de departamentos únicos
     */
    @Query("SELECT DISTINCT u.departamento FROM Usuario u WHERE u.departamento IS NOT NULL ORDER BY u.departamento")
    List<String> findAllDepartamentos();
    
    /**
     * Verifica si existe un usuario con el email dado
     * @param email El email a verificar
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);
    
    /**
     * Verifica si existe un usuario con el Azure Object ID dado
     * @param azureObjectId El Object ID a verificar
     * @return true si existe, false si no
     */
    boolean existsByAzureObjectId(String azureObjectId);
}
