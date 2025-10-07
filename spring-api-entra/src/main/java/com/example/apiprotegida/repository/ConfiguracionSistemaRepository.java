package com.example.apiprotegida.repository;

import com.example.apiprotegida.model.ConfiguracionSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar configuraciones del sistema
 */
@Repository
public interface ConfiguracionSistemaRepository extends JpaRepository<ConfiguracionSistema, Long> {
    
    /**
     * Busca una configuración por su clave
     */
    Optional<ConfiguracionSistema> findByClave(String clave);
    
    /**
     * Busca configuraciones por categoría
     */
    List<ConfiguracionSistema> findByCategoria(String categoria);
    
    /**
     * Busca configuraciones activas
     */
    List<ConfiguracionSistema> findByActivoTrue();
    
    /**
     * Busca configuraciones activas por categoría
     */
    List<ConfiguracionSistema> findByCategoriaAndActivoTrue(String categoria);
}


