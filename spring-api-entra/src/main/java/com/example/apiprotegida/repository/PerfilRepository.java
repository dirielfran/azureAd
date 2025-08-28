package com.example.apiprotegida.repository;

import com.example.apiprotegida.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Perfil
 */
@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    
    /**
     * Busca un perfil por su nombre
     */
    Optional<Perfil> findByNombre(String nombre);
    
    /**
     * Busca un perfil por el ID del grupo de Azure AD
     */
    Optional<Perfil> findByAzureGroupId(String azureGroupId);
    
    /**
     * Busca un perfil por el nombre del grupo de Azure AD
     */
    Optional<Perfil> findByAzureGroupName(String azureGroupName);
    
    /**
     * Busca todos los perfiles activos
     */
    List<Perfil> findByActivoTrue();
    
    /**
     * Busca perfiles por múltiples IDs de grupos de Azure AD
     */
    @Query("SELECT p FROM Perfil p WHERE p.azureGroupId IN :azureGroupIds AND p.activo = true")
    List<Perfil> findByAzureGroupIds(@Param("azureGroupIds") List<String> azureGroupIds);
    
    /**
     * Busca perfiles con sus permisos cargados
     */
    @Query("SELECT DISTINCT p FROM Perfil p LEFT JOIN FETCH p.permisos WHERE p.activo = true")
    List<Perfil> findAllWithPermisos();
    
    /**
     * Busca un perfil con sus permisos por ID de grupo de Azure
     */
    @Query("SELECT DISTINCT p FROM Perfil p LEFT JOIN FETCH p.permisos WHERE p.azureGroupId = :azureGroupId AND p.activo = true")
    Optional<Perfil> findByAzureGroupIdWithPermisos(@Param("azureGroupId") String azureGroupId);
    
    /**
     * Verifica si existe un perfil con el nombre dado
     */
    boolean existsByNombre(String nombre);
    
    /**
     * Verifica si existe un perfil con el ID de grupo de Azure dado
     */
    boolean existsByAzureGroupId(String azureGroupId);
}
