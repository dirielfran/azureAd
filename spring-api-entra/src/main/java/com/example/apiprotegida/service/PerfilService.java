package com.example.apiprotegida.service;

import com.example.apiprotegida.model.Perfil;
import com.example.apiprotegida.model.Permiso;
import com.example.apiprotegida.repository.PerfilRepository;
import com.example.apiprotegida.repository.PermisoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Servicio para gestionar perfiles del sistema
 */
@Service
@Transactional
public class PerfilService {

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private PermisoRepository permisoRepository;

    /**
     * Obtiene todos los perfiles activos
     */
    @Transactional(readOnly = true)
    public List<Perfil> obtenerTodosLosPerfiles() {
        return perfilRepository.findByActivoTrue();
    }

    /**
     * Obtiene todos los perfiles con sus permisos cargados
     */
    @Transactional(readOnly = true)
    public List<Perfil> obtenerPerfilesConPermisos() {
        return perfilRepository.findAllWithPermisos();
    }

    /**
     * Busca un perfil por ID
     */
    @Transactional(readOnly = true)
    public Optional<Perfil> obtenerPerfilPorId(Long id) {
        return perfilRepository.findById(id);
    }

    /**
     * Busca un perfil por nombre
     */
    @Transactional(readOnly = true)
    public Optional<Perfil> obtenerPerfilPorNombre(String nombre) {
        return perfilRepository.findByNombre(nombre);
    }

    /**
     * Busca un perfil por ID de grupo de Azure AD
     */
    @Transactional(readOnly = true)
    public Optional<Perfil> obtenerPerfilPorAzureGroupId(String azureGroupId) {
        return perfilRepository.findByAzureGroupId(azureGroupId);
    }

    /**
     * Busca un perfil por ID de grupo de Azure AD con permisos cargados
     */
    @Transactional(readOnly = true)
    public Optional<Perfil> obtenerPerfilPorAzureGroupIdConPermisos(String azureGroupId) {
        return perfilRepository.findByAzureGroupIdWithPermisos(azureGroupId);
    }

    /**
     * Busca perfiles por múltiples IDs de grupos de Azure AD
     */
    @Transactional(readOnly = true)
    public List<Perfil> obtenerPerfilesPorAzureGroupIds(List<String> azureGroupIds) {
        return perfilRepository.findByAzureGroupIds(azureGroupIds);
    }

    /**
     * Crea un nuevo perfil
     */
    public Perfil crearPerfil(Perfil perfil) {
        if (perfilRepository.existsByNombre(perfil.getNombre())) {
            throw new IllegalArgumentException("Ya existe un perfil con el nombre: " + perfil.getNombre());
        }
        if (perfil.getAzureGroupId() != null && perfilRepository.existsByAzureGroupId(perfil.getAzureGroupId())) {
            throw new IllegalArgumentException("Ya existe un perfil asociado al grupo de Azure: " + perfil.getAzureGroupId());
        }
        return perfilRepository.save(perfil);
    }

    /**
     * Actualiza un perfil existente
     */
    public Perfil actualizarPerfil(Long id, Perfil perfilActualizado) {
        Perfil perfilExistente = perfilRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado con ID: " + id));

        // Verificar si el nuevo nombre ya existe en otro perfil
        if (!perfilExistente.getNombre().equals(perfilActualizado.getNombre()) &&
            perfilRepository.existsByNombre(perfilActualizado.getNombre())) {
            throw new IllegalArgumentException("Ya existe un perfil con el nombre: " + perfilActualizado.getNombre());
        }

        // Verificar si el nuevo Azure Group ID ya existe en otro perfil
        if (perfilActualizado.getAzureGroupId() != null &&
            !perfilActualizado.getAzureGroupId().equals(perfilExistente.getAzureGroupId()) &&
            perfilRepository.existsByAzureGroupId(perfilActualizado.getAzureGroupId())) {
            throw new IllegalArgumentException("Ya existe un perfil asociado al grupo de Azure: " + perfilActualizado.getAzureGroupId());
        }

        perfilExistente.setNombre(perfilActualizado.getNombre());
        perfilExistente.setDescripcion(perfilActualizado.getDescripcion());
        perfilExistente.setAzureGroupId(perfilActualizado.getAzureGroupId());
        perfilExistente.setAzureGroupName(perfilActualizado.getAzureGroupName());
        perfilExistente.setActivo(perfilActualizado.getActivo());

        return perfilRepository.save(perfilExistente);
    }

    /**
     * Desactiva un perfil (eliminación lógica)
     */
    public void desactivarPerfil(Long id) {
        Perfil perfil = perfilRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado con ID: " + id));
        perfil.setActivo(false);
        perfilRepository.save(perfil);
    }

    /**
     * Elimina un perfil permanentemente
     */
    public void eliminarPerfil(Long id) {
        if (!perfilRepository.existsById(id)) {
            throw new IllegalArgumentException("Perfil no encontrado con ID: " + id);
        }
        perfilRepository.deleteById(id);
    }

    /**
     * Asigna un permiso a un perfil
     */
    public Perfil asignarPermiso(Long perfilId, Long permisoId) {
        Perfil perfil = perfilRepository.findById(perfilId)
            .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado con ID: " + perfilId));

        Permiso permiso = permisoRepository.findById(permisoId)
            .orElseThrow(() -> new IllegalArgumentException("Permiso no encontrado con ID: " + permisoId));

        perfil. addPermiso(permiso);
        return perfilRepository.save(perfil);
    }

    /**
     * Remueve un permiso de un perfil
     */
    public Perfil removerPermiso(Long perfilId, Long permisoId) {
        Perfil perfil = perfilRepository.findById(perfilId)
            .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado con ID: " + perfilId));

        Permiso permiso = permisoRepository.findById(permisoId)
            .orElseThrow(() -> new IllegalArgumentException("Permiso no encontrado con ID: " + permisoId));

        perfil.removePermiso(permiso);
        return perfilRepository.save(perfil);
    }

    /**
     * Asigna múltiples permisos a un perfil
     */
    public Perfil asignarPermisos(Long perfilId, Set<Long> permisoIds) {
        Perfil perfil = perfilRepository.findById(perfilId)
            .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado con ID: " + perfilId));

        List<Permiso> permisos = permisoRepository.findAllById(permisoIds);
        if (permisos.size() != permisoIds.size()) {
            throw new IllegalArgumentException("Algunos permisos no fueron encontrados");
        }

        for (Permiso permiso : permisos) {
            perfil.addPermiso(permiso);
        }

        return perfilRepository.save(perfil);
    }

    /**
     * Reemplaza todos los permisos de un perfil
     */
    public Perfil reemplazarPermisos(Long perfilId, Set<Long> permisoIds) {
        Perfil perfil = perfilRepository.findById(perfilId)
            .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado con ID: " + perfilId));

        // Limpiar permisos existentes
        perfil.getPermisos().clear();

        // Agregar nuevos permisos
        if (permisoIds != null && !permisoIds.isEmpty()) {
            List<Permiso> permisos = permisoRepository.findAllById(permisoIds);
            if (permisos.size() != permisoIds.size()) {
                throw new IllegalArgumentException("Algunos permisos no fueron encontrados");
            }

            for (Permiso permiso : permisos) {
                perfil.addPermiso(permiso);
            }
        }

        return perfilRepository.save(perfil);
    }
}
