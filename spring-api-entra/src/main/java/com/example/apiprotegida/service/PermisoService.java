package com.example.apiprotegida.service;

import com.example.apiprotegida.model.Permiso;
import com.example.apiprotegida.repository.PermisoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Servicio para gestionar permisos del sistema
 */
@Service
@Transactional
public class PermisoService {

    @Autowired
    private PermisoRepository permisoRepository;

    /**
     * Obtiene todos los permisos activos
     */
    @Transactional(readOnly = true)
    public List<Permiso> obtenerTodosLosPermisos() {
        return permisoRepository.findByActivoTrue();
    }

    /**
     * Busca un permiso por ID
     */
    @Transactional(readOnly = true)
    public Optional<Permiso> obtenerPermisoPorId(Long id) {
        return permisoRepository.findById(id);
    }

    /**
     * Busca un permiso por código
     */
    @Transactional(readOnly = true)
    public Optional<Permiso> obtenerPermisoPorCodigo(String codigo) {
        return permisoRepository.findByCodigo(codigo);
    }

    /**
     * Obtiene permisos por módulo
     */
    @Transactional(readOnly = true)
    public List<Permiso> obtenerPermisosPorModulo(String modulo) {
        return permisoRepository.findByModuloAndActivoTrue(modulo);
    }

    /**
     * Obtiene permisos por acción
     */
    @Transactional(readOnly = true)
    public List<Permiso> obtenerPermisosPorAccion(String accion) {
        return permisoRepository.findByAccionAndActivoTrue(accion);
    }

    /**
     * Obtiene permisos por módulo y acción
     */
    @Transactional(readOnly = true)
    public List<Permiso> obtenerPermisosPorModuloYAccion(String modulo, String accion) {
        return permisoRepository.findByModuloAndAccionAndActivoTrue(modulo, accion);
    }

    /**
     * Obtiene permisos por múltiples códigos
     */
    @Transactional(readOnly = true)
    public List<Permiso> obtenerPermisosPorCodigos(Set<String> codigos) {
        return permisoRepository.findByCodigoInAndActivoTrue(codigos);
    }

    /**
     * Obtiene todos los módulos únicos
     */
    @Transactional(readOnly = true)
    public List<String> obtenerModulos() {
        return permisoRepository.findDistinctModulos();
    }

    /**
     * Obtiene todas las acciones únicas
     */
    @Transactional(readOnly = true)
    public List<String> obtenerAcciones() {
        return permisoRepository.findDistinctAcciones();
    }

    /**
     * Obtiene permisos asociados a un perfil
     */
    @Transactional(readOnly = true)
    public List<Permiso> obtenerPermisosPorPerfilId(Long perfilId) {
        return permisoRepository.findByPerfilId(perfilId);
    }

    /**
     * Obtiene permisos asociados a múltiples perfiles
     */
    @Transactional(readOnly = true)
    public List<Permiso> obtenerPermisosPorPerfilIds(List<Long> perfilIds) {
        return permisoRepository.findByPerfilIds(perfilIds);
    }

    /**
     * Crea un nuevo permiso
     */
    public Permiso crearPermiso(Permiso permiso) {
        if (permisoRepository.existsByCodigo(permiso.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un permiso con el código: " + permiso.getCodigo());
        }
        return permisoRepository.save(permiso);
    }

    /**
     * Actualiza un permiso existente
     */
    public Permiso actualizarPermiso(Long id, Permiso permisoActualizado) {
        Permiso permisoExistente = permisoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Permiso no encontrado con ID: " + id));

        // Verificar si el nuevo código ya existe en otro permiso
        if (!permisoExistente.getCodigo().equals(permisoActualizado.getCodigo()) &&
            permisoRepository.existsByCodigo(permisoActualizado.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un permiso con el código: " + permisoActualizado.getCodigo());
        }

        permisoExistente.setCodigo(permisoActualizado.getCodigo());
        permisoExistente.setNombre(permisoActualizado.getNombre());
        permisoExistente.setDescripcion(permisoActualizado.getDescripcion());
        permisoExistente.setModulo(permisoActualizado.getModulo());
        permisoExistente.setAccion(permisoActualizado.getAccion());
        permisoExistente.setActivo(permisoActualizado.getActivo());

        return permisoRepository.save(permisoExistente);
    }

    /**
     * Desactiva un permiso (eliminación lógica)
     */
    public void desactivarPermiso(Long id) {
        Permiso permiso = permisoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Permiso no encontrado con ID: " + id));
        permiso.setActivo(false);
        permisoRepository.save(permiso);
    }

    /**
     * Elimina un permiso permanentemente
     */
    public void eliminarPermiso(Long id) {
        if (!permisoRepository.existsById(id)) {
            throw new IllegalArgumentException("Permiso no encontrado con ID: " + id);
        }
        permisoRepository.deleteById(id);
    }

    /**
     * Obtiene estadísticas de permisos por módulo
     */
    @Transactional(readOnly = true)
    public List<Object[]> obtenerEstadisticasPorModulo() {
        return permisoRepository.countPermisosByModulo();
    }

    /**
     * Verifica si existe un permiso con el código dado
     */
    @Transactional(readOnly = true)
    public boolean existePermiso(String codigo) {
        return permisoRepository.existsByCodigo(codigo);
    }
}
