package com.example.apiprotegida.repository;

import com.example.apiprotegida.model.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repositorio para la entidad Permiso
 */
@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {
    
    /**
     * Busca un permiso por su código
     */
    Optional<Permiso> findByCodigo(String codigo);
    
    /**
     * Busca todos los permisos activos
     */
    List<Permiso> findByActivoTrue();
    
    /**
     * Busca permisos por módulo
     */
    List<Permiso> findByModuloAndActivoTrue(String modulo);
    
    /**
     * Busca permisos por acción
     */
    List<Permiso> findByAccionAndActivoTrue(String accion);
    
    /**
     * Busca permisos por módulo y acción
     */
    List<Permiso> findByModuloAndAccionAndActivoTrue(String modulo, String accion);
    
    /**
     * Busca permisos por múltiples códigos
     */
    List<Permiso> findByCodigoInAndActivoTrue(Set<String> codigos);
    
    /**
     * Obtiene todos los módulos únicos
     */
    @Query("SELECT DISTINCT p.modulo FROM Permiso p WHERE p.activo = true ORDER BY p.modulo")
    List<String> findDistinctModulos();
    
    /**
     * Obtiene todas las acciones únicas
     */
    @Query("SELECT DISTINCT p.accion FROM Permiso p WHERE p.activo = true ORDER BY p.accion")
    List<String> findDistinctAcciones();
    
    /**
     * Busca permisos asociados a un perfil específico
     */
    @Query("SELECT p FROM Permiso p JOIN p.perfiles pf WHERE pf.id = :perfilId AND p.activo = true")
    List<Permiso> findByPerfilId(@Param("perfilId") Long perfilId);
    
    /**
     * Busca permisos asociados a múltiples perfiles
     */
    @Query("SELECT DISTINCT p FROM Permiso p JOIN p.perfiles pf WHERE pf.id IN :perfilIds AND p.activo = true")
    List<Permiso> findByPerfilIds(@Param("perfilIds") List<Long> perfilIds);
    
    /**
     * Verifica si existe un permiso con el código dado
     */
    boolean existsByCodigo(String codigo);
    
    /**
     * Cuenta permisos por módulo
     */
    @Query("SELECT p.modulo, COUNT(p) FROM Permiso p WHERE p.activo = true GROUP BY p.modulo")
    List<Object[]> countPermisosByModulo();
}
