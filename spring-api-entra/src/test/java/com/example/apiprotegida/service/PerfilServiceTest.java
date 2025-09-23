package com.example.apiprotegida.service;

import com.example.apiprotegida.model.Perfil;
import com.example.apiprotegida.model.Permiso;
import com.example.apiprotegida.repository.PerfilRepository;
import com.example.apiprotegida.repository.PermisoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PerfilService
 */
@ExtendWith(MockitoExtension.class)
class PerfilServiceTest {

    @Mock
    private PerfilRepository perfilRepository;

    @Mock
    private PermisoRepository permisoRepository;

    @InjectMocks
    private PerfilService perfilService;

    private Perfil perfilTest;
    private Permiso permisoTest;

    @BeforeEach
    void setUp() {
        // Crear datos de prueba
        permisoTest = new Permiso("USUARIOS_LEER", "Leer Usuarios", "Permiso para leer usuarios", "USUARIOS", "LEER");
        permisoTest.setId(1L);
        permisoTest.setActivo(true);

        perfilTest = new Perfil("Administrador", "Perfil de administrador", "admin-group-id", "Admin Group");
        perfilTest.setId(1L);
        perfilTest.setActivo(true);
        perfilTest.addPermiso(permisoTest);
    }

    // ========== TESTS PARA MÉTODOS DE CONSULTA ==========

    @Test
    void obtenerTodosLosPerfiles_DeberiaRetornarListaDePerfiles() {
        // Arrange
        List<Perfil> perfilesEsperados = Arrays.asList(perfilTest);
        when(perfilRepository.findByActivoTrue()).thenReturn(perfilesEsperados);

        // Act
        List<Perfil> resultado = perfilService.obtenerTodosLosPerfiles();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Administrador", resultado.get(0).getNombre());
        verify(perfilRepository).findByActivoTrue();
    }

    @Test
    void obtenerPerfilesConPermisos_DeberiaRetornarPerfilesConPermisosCargados() {
        // Arrange
        List<Perfil> perfilesEsperados = Arrays.asList(perfilTest);
        when(perfilRepository.findAllWithPermisos()).thenReturn(perfilesEsperados);

        // Act
        List<Perfil> resultado = perfilService.obtenerPerfilesConPermisos();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Administrador", resultado.get(0).getNombre());
        verify(perfilRepository).findAllWithPermisos();
    }

    @Test
    void obtenerPerfilPorId_ConIdValido_DeberiaRetornarPerfil() {
        // Arrange
        Long id = 1L;
        when(perfilRepository.findById(id)).thenReturn(Optional.of(perfilTest));

        // Act
        Optional<Perfil> resultado = perfilService.obtenerPerfilPorId(id);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Administrador", resultado.get().getNombre());
        verify(perfilRepository).findById(id);
    }

    @Test
    void obtenerPerfilPorId_ConIdInvalido_DeberiaRetornarVacio() {
        // Arrange
        Long id = 999L;
        when(perfilRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<Perfil> resultado = perfilService.obtenerPerfilPorId(id);

        // Assert
        assertFalse(resultado.isPresent());
        verify(perfilRepository).findById(id);
    }

    @Test
    void obtenerPerfilPorNombre_ConNombreValido_DeberiaRetornarPerfil() {
        // Arrange
        String nombre = "Administrador";
        when(perfilRepository.findByNombre(nombre)).thenReturn(Optional.of(perfilTest));

        // Act
        Optional<Perfil> resultado = perfilService.obtenerPerfilPorNombre(nombre);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Administrador", resultado.get().getNombre());
        verify(perfilRepository).findByNombre(nombre);
    }

    @Test
    void obtenerPerfilPorNombre_ConNombreInvalido_DeberiaRetornarVacio() {
        // Arrange
        String nombre = "PerfilInexistente";
        when(perfilRepository.findByNombre(nombre)).thenReturn(Optional.empty());

        // Act
        Optional<Perfil> resultado = perfilService.obtenerPerfilPorNombre(nombre);

        // Assert
        assertFalse(resultado.isPresent());
        verify(perfilRepository).findByNombre(nombre);
    }

    @Test
    void obtenerPerfilPorAzureGroupId_ConGroupIdValido_DeberiaRetornarPerfil() {
        // Arrange
        String azureGroupId = "admin-group-id";
        when(perfilRepository.findByAzureGroupId(azureGroupId)).thenReturn(Optional.of(perfilTest));

        // Act
        Optional<Perfil> resultado = perfilService.obtenerPerfilPorAzureGroupId(azureGroupId);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("admin-group-id", resultado.get().getAzureGroupId());
        verify(perfilRepository).findByAzureGroupId(azureGroupId);
    }

    @Test
    void obtenerPerfilPorAzureGroupId_ConGroupIdInvalido_DeberiaRetornarVacio() {
        // Arrange
        String azureGroupId = "group-inexistente";
        when(perfilRepository.findByAzureGroupId(azureGroupId)).thenReturn(Optional.empty());

        // Act
        Optional<Perfil> resultado = perfilService.obtenerPerfilPorAzureGroupId(azureGroupId);

        // Assert
        assertFalse(resultado.isPresent());
        verify(perfilRepository).findByAzureGroupId(azureGroupId);
    }

    @Test
    void obtenerPerfilPorAzureGroupIdConPermisos_ConGroupIdValido_DeberiaRetornarPerfilConPermisos() {
        // Arrange
        String azureGroupId = "admin-group-id";
        when(perfilRepository.findByAzureGroupIdWithPermisos(azureGroupId)).thenReturn(Optional.of(perfilTest));

        // Act
        Optional<Perfil> resultado = perfilService.obtenerPerfilPorAzureGroupIdConPermisos(azureGroupId);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("admin-group-id", resultado.get().getAzureGroupId());
        verify(perfilRepository).findByAzureGroupIdWithPermisos(azureGroupId);
    }

    @Test
    void obtenerPerfilesPorAzureGroupIds_ConGroupIdsValidos_DeberiaRetornarPerfiles() {
        // Arrange
        List<String> azureGroupIds = Arrays.asList("admin-group-id", "user-group-id");
        List<Perfil> perfilesEsperados = Arrays.asList(perfilTest);
        when(perfilRepository.findByAzureGroupIds(azureGroupIds)).thenReturn(perfilesEsperados);

        // Act
        List<Perfil> resultado = perfilService.obtenerPerfilesPorAzureGroupIds(azureGroupIds);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(perfilRepository).findByAzureGroupIds(azureGroupIds);
    }

    // ========== TESTS PARA MÉTODOS DE CREACIÓN ==========

    @Test
    void crearPerfil_ConDatosValidos_DeberiaCrearPerfil() {
        // Arrange
        Perfil nuevoPerfil = new Perfil("Nuevo Perfil", "Descripción del nuevo perfil", "new-group-id", "New Group");
        when(perfilRepository.existsByNombre("Nuevo Perfil")).thenReturn(false);
        when(perfilRepository.existsByAzureGroupId("new-group-id")).thenReturn(false);
        when(perfilRepository.save(any(Perfil.class))).thenReturn(nuevoPerfil);

        // Act
        Perfil resultado = perfilService.crearPerfil(nuevoPerfil);

        // Assert
        assertNotNull(resultado);
        assertEquals("Nuevo Perfil", resultado.getNombre());
        verify(perfilRepository).existsByNombre("Nuevo Perfil");
        verify(perfilRepository).existsByAzureGroupId("new-group-id");
        verify(perfilRepository).save(nuevoPerfil);
    }

    @Test
    void crearPerfil_ConNombreDuplicado_DeberiaLanzarExcepcion() {
        // Arrange
        Perfil nuevoPerfil = new Perfil("Administrador", "Descripción", "new-group-id", "New Group");
        when(perfilRepository.existsByNombre("Administrador")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, 
            () -> perfilService.crearPerfil(nuevoPerfil));
        
        assertEquals("Ya existe un perfil con el nombre: Administrador", excepcion.getMessage());
        verify(perfilRepository).existsByNombre("Administrador");
        verify(perfilRepository, never()).save(any(Perfil.class));
    }

    @Test
    void crearPerfil_ConAzureGroupIdDuplicado_DeberiaLanzarExcepcion() {
        // Arrange
        Perfil nuevoPerfil = new Perfil("Nuevo Perfil", "Descripción", "admin-group-id", "Admin Group");
        when(perfilRepository.existsByNombre("Nuevo Perfil")).thenReturn(false);
        when(perfilRepository.existsByAzureGroupId("admin-group-id")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, 
            () -> perfilService.crearPerfil(nuevoPerfil));
        
        assertEquals("Ya existe un perfil asociado al grupo de Azure: admin-group-id", excepcion.getMessage());
        verify(perfilRepository).existsByNombre("Nuevo Perfil");
        verify(perfilRepository).existsByAzureGroupId("admin-group-id");
        verify(perfilRepository, never()).save(any(Perfil.class));
    }

    @Test
    void crearPerfil_ConAzureGroupIdNull_DeberiaCrearPerfil() {
        // Arrange
        Perfil nuevoPerfil = new Perfil("Nuevo Perfil", "Descripción", null, null);
        when(perfilRepository.existsByNombre("Nuevo Perfil")).thenReturn(false);
        when(perfilRepository.save(any(Perfil.class))).thenReturn(nuevoPerfil);

        // Act
        Perfil resultado = perfilService.crearPerfil(nuevoPerfil);

        // Assert
        assertNotNull(resultado);
        assertEquals("Nuevo Perfil", resultado.getNombre());
        verify(perfilRepository).existsByNombre("Nuevo Perfil");
        verify(perfilRepository, never()).existsByAzureGroupId(any());
        verify(perfilRepository).save(nuevoPerfil);
    }

    // ========== TESTS PARA MÉTODOS DE ACTUALIZACIÓN ==========

    @Test
    void actualizarPerfil_ConDatosValidos_DeberiaActualizarPerfil() {
        // Arrange
        Long id = 1L;
        Perfil perfilActualizado = new Perfil("Administrador Actualizado", "Nueva descripción", "admin-group-id", "Admin Group");
        perfilActualizado.setActivo(true);
        
        when(perfilRepository.findById(id)).thenReturn(Optional.of(perfilTest));
        when(perfilRepository.existsByNombre("Administrador Actualizado")).thenReturn(false);
        when(perfilRepository.save(any(Perfil.class))).thenReturn(perfilActualizado);

        // Act
        Perfil resultado = perfilService.actualizarPerfil(id, perfilActualizado);

        // Assert
        assertNotNull(resultado);
        assertEquals("Administrador Actualizado", resultado.getNombre());
        verify(perfilRepository).findById(id);
        verify(perfilRepository).existsByNombre("Administrador Actualizado");
        // No se verifica existsByAzureGroupId porque el azureGroupId es el mismo
        verify(perfilRepository).save(any(Perfil.class));
    }

    @Test
    void actualizarPerfil_ConIdInexistente_DeberiaLanzarExcepcion() {
        // Arrange
        Long id = 999L;
        Perfil perfilActualizado = new Perfil("Administrador Actualizado", "Nueva descripción", "admin-group-id", "Admin Group");
        when(perfilRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, 
            () -> perfilService.actualizarPerfil(id, perfilActualizado));
        
        assertEquals("Perfil no encontrado con ID: 999", excepcion.getMessage());
        verify(perfilRepository).findById(id);
        verify(perfilRepository, never()).save(any(Perfil.class));
    }

    @Test
    void actualizarPerfil_ConNombreDuplicado_DeberiaLanzarExcepcion() {
        // Arrange
        Long id = 1L;
        Perfil perfilActualizado = new Perfil("Otro Perfil", "Nueva descripción", "admin-group-id", "Admin Group");
        when(perfilRepository.findById(id)).thenReturn(Optional.of(perfilTest));
        when(perfilRepository.existsByNombre("Otro Perfil")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, 
            () -> perfilService.actualizarPerfil(id, perfilActualizado));
        
        assertEquals("Ya existe un perfil con el nombre: Otro Perfil", excepcion.getMessage());
        verify(perfilRepository).findById(id);
        verify(perfilRepository).existsByNombre("Otro Perfil");
        verify(perfilRepository, never()).save(any(Perfil.class));
    }

    @Test
    void actualizarPerfil_ConMismoNombre_DeberiaActualizarPerfil() {
        // Arrange
        Long id = 1L;
        Perfil perfilActualizado = new Perfil("Administrador", "Nueva descripción", "admin-group-id", "Admin Group");
        perfilActualizado.setActivo(true);
        
        when(perfilRepository.findById(id)).thenReturn(Optional.of(perfilTest));
        when(perfilRepository.save(any(Perfil.class))).thenReturn(perfilActualizado);

        // Act
        Perfil resultado = perfilService.actualizarPerfil(id, perfilActualizado);

        // Assert
        assertNotNull(resultado);
        assertEquals("Administrador", resultado.getNombre());
        verify(perfilRepository).findById(id);
        verify(perfilRepository, never()).existsByNombre(any());
        // No se verifica existsByAzureGroupId porque el azureGroupId es el mismo
        verify(perfilRepository).save(any(Perfil.class));
    }

    @Test
    void actualizarPerfil_ConAzureGroupIdDiferente_DeberiaVerificarDuplicado() {
        // Arrange
        Long id = 1L;
        Perfil perfilActualizado = new Perfil("Administrador", "Nueva descripción", "new-group-id", "New Group");
        perfilActualizado.setActivo(true);
        
        when(perfilRepository.findById(id)).thenReturn(Optional.of(perfilTest));
        when(perfilRepository.existsByAzureGroupId("new-group-id")).thenReturn(false);
        when(perfilRepository.save(any(Perfil.class))).thenReturn(perfilActualizado);

        // Act
        Perfil resultado = perfilService.actualizarPerfil(id, perfilActualizado);

        // Assert
        assertNotNull(resultado);
        assertEquals("Administrador", resultado.getNombre());
        verify(perfilRepository).findById(id);
        verify(perfilRepository, never()).existsByNombre(any());
        verify(perfilRepository).existsByAzureGroupId("new-group-id");
        verify(perfilRepository).save(any(Perfil.class));
    }

    // ========== TESTS PARA MÉTODOS DE ELIMINACIÓN ==========

    @Test
    void desactivarPerfil_ConIdValido_DeberiaDesactivarPerfil() {
        // Arrange
        Long id = 1L;
        when(perfilRepository.findById(id)).thenReturn(Optional.of(perfilTest));
        when(perfilRepository.save(any(Perfil.class))).thenReturn(perfilTest);

        // Act
        perfilService.desactivarPerfil(id);

        // Assert
        assertFalse(perfilTest.getActivo());
        verify(perfilRepository).findById(id);
        verify(perfilRepository).save(perfilTest);
    }

    @Test
    void desactivarPerfil_ConIdInexistente_DeberiaLanzarExcepcion() {
        // Arrange
        Long id = 999L;
        when(perfilRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, 
            () -> perfilService.desactivarPerfil(id));
        
        assertEquals("Perfil no encontrado con ID: 999", excepcion.getMessage());
        verify(perfilRepository).findById(id);
        verify(perfilRepository, never()).save(any(Perfil.class));
    }

    @Test
    void eliminarPerfil_ConIdValido_DeberiaEliminarPerfil() {
        // Arrange
        Long id = 1L;
        when(perfilRepository.existsById(id)).thenReturn(true);

        // Act
        perfilService.eliminarPerfil(id);

        // Assert
        verify(perfilRepository).existsById(id);
        verify(perfilRepository).deleteById(id);
    }

    @Test
    void eliminarPerfil_ConIdInexistente_DeberiaLanzarExcepcion() {
        // Arrange
        Long id = 999L;
        when(perfilRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, 
            () -> perfilService.eliminarPerfil(id));
        
        assertEquals("Perfil no encontrado con ID: 999", excepcion.getMessage());
        verify(perfilRepository).existsById(id);
        verify(perfilRepository, never()).deleteById(any());
    }

    // ========== TESTS PARA MÉTODOS DE GESTIÓN DE PERMISOS ==========

    @Test
    void asignarPermiso_ConIdsValidos_DeberiaAsignarPermiso() {
        // Arrange
        Long perfilId = 1L;
        Long permisoId = 2L;
        Permiso nuevoPermiso = new Permiso("USUARIOS_CREAR", "Crear Usuarios", "Permiso para crear usuarios", "USUARIOS", "CREAR");
        nuevoPermiso.setId(2L);
        
        when(perfilRepository.findById(perfilId)).thenReturn(Optional.of(perfilTest));
        when(permisoRepository.findById(permisoId)).thenReturn(Optional.of(nuevoPermiso));
        when(perfilRepository.save(any(Perfil.class))).thenReturn(perfilTest);

        // Act
        Perfil resultado = perfilService.asignarPermiso(perfilId, permisoId);

        // Assert
        assertNotNull(resultado);
        verify(perfilRepository).findById(perfilId);
        verify(permisoRepository).findById(permisoId);
        verify(perfilRepository).save(perfilTest);
    }

    @Test
    void asignarPermiso_ConPerfilInexistente_DeberiaLanzarExcepcion() {
        // Arrange
        Long perfilId = 999L;
        Long permisoId = 2L;
        when(perfilRepository.findById(perfilId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, 
            () -> perfilService.asignarPermiso(perfilId, permisoId));
        
        assertEquals("Perfil no encontrado con ID: 999", excepcion.getMessage());
        verify(perfilRepository).findById(perfilId);
        verify(permisoRepository, never()).findById(any());
        verify(perfilRepository, never()).save(any(Perfil.class));
    }

    @Test
    void asignarPermiso_ConPermisoInexistente_DeberiaLanzarExcepcion() {
        // Arrange
        Long perfilId = 1L;
        Long permisoId = 999L;
        when(perfilRepository.findById(perfilId)).thenReturn(Optional.of(perfilTest));
        when(permisoRepository.findById(permisoId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, 
            () -> perfilService.asignarPermiso(perfilId, permisoId));
        
        assertEquals("Permiso no encontrado con ID: 999", excepcion.getMessage());
        verify(perfilRepository).findById(perfilId);
        verify(permisoRepository).findById(permisoId);
        verify(perfilRepository, never()).save(any(Perfil.class));
    }

    @Test
    void removerPermiso_ConIdsValidos_DeberiaRemoverPermiso() {
        // Arrange
        Long perfilId = 1L;
        Long permisoId = 1L;
        when(perfilRepository.findById(perfilId)).thenReturn(Optional.of(perfilTest));
        when(permisoRepository.findById(permisoId)).thenReturn(Optional.of(permisoTest));
        when(perfilRepository.save(any(Perfil.class))).thenReturn(perfilTest);

        // Act
        Perfil resultado = perfilService.removerPermiso(perfilId, permisoId);

        // Assert
        assertNotNull(resultado);
        verify(perfilRepository).findById(perfilId);
        verify(permisoRepository).findById(permisoId);
        verify(perfilRepository).save(perfilTest);
    }

    @Test
    void asignarPermisos_ConIdsValidos_DeberiaAsignarPermisos() {
        // Arrange
        Long perfilId = 1L;
        Set<Long> permisoIds = Set.of(1L, 2L);
        List<Permiso> permisos = Arrays.asList(permisoTest, new Permiso("USUARIOS_CREAR", "Crear Usuarios", "Permiso para crear usuarios", "USUARIOS", "CREAR"));
        
        when(perfilRepository.findById(perfilId)).thenReturn(Optional.of(perfilTest));
        when(permisoRepository.findAllById(permisoIds)).thenReturn(permisos);
        when(perfilRepository.save(any(Perfil.class))).thenReturn(perfilTest);

        // Act
        Perfil resultado = perfilService.asignarPermisos(perfilId, permisoIds);

        // Assert
        assertNotNull(resultado);
        verify(perfilRepository).findById(perfilId);
        verify(permisoRepository).findAllById(permisoIds);
        verify(perfilRepository).save(perfilTest);
    }

    @Test
    void asignarPermisos_ConPermisosInexistentes_DeberiaLanzarExcepcion() {
        // Arrange
        Long perfilId = 1L;
        Set<Long> permisoIds = Set.of(1L, 999L);
        List<Permiso> permisos = Arrays.asList(permisoTest); // Solo un permiso encontrado
        
        when(perfilRepository.findById(perfilId)).thenReturn(Optional.of(perfilTest));
        when(permisoRepository.findAllById(permisoIds)).thenReturn(permisos);

        // Act & Assert
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, 
            () -> perfilService.asignarPermisos(perfilId, permisoIds));
        
        assertEquals("Algunos permisos no fueron encontrados", excepcion.getMessage());
        verify(perfilRepository).findById(perfilId);
        verify(permisoRepository).findAllById(permisoIds);
        verify(perfilRepository, never()).save(any(Perfil.class));
    }

    @Test
    void reemplazarPermisos_ConIdsValidos_DeberiaReemplazarPermisos() {
        // Arrange
        Long perfilId = 1L;
        Set<Long> permisoIds = Set.of(2L, 3L);
        List<Permiso> permisos = Arrays.asList(
            new Permiso("USUARIOS_CREAR", "Crear Usuarios", "Permiso para crear usuarios", "USUARIOS", "CREAR"),
            new Permiso("REPORTES_LEER", "Leer Reportes", "Permiso para leer reportes", "REPORTES", "LEER")
        );
        
        when(perfilRepository.findById(perfilId)).thenReturn(Optional.of(perfilTest));
        when(permisoRepository.findAllById(permisoIds)).thenReturn(permisos);
        when(perfilRepository.save(any(Perfil.class))).thenReturn(perfilTest);

        // Act
        Perfil resultado = perfilService.reemplazarPermisos(perfilId, permisoIds);

        // Assert
        assertNotNull(resultado);
        verify(perfilRepository).findById(perfilId);
        verify(permisoRepository).findAllById(permisoIds);
        verify(perfilRepository).save(perfilTest);
    }

    @Test
    void reemplazarPermisos_ConPermisosNull_DeberiaLimpiarPermisos() {
        // Arrange
        Long perfilId = 1L;
        Set<Long> permisoIds = null;
        
        when(perfilRepository.findById(perfilId)).thenReturn(Optional.of(perfilTest));
        when(perfilRepository.save(any(Perfil.class))).thenReturn(perfilTest);

        // Act
        Perfil resultado = perfilService.reemplazarPermisos(perfilId, permisoIds);

        // Assert
        assertNotNull(resultado);
        verify(perfilRepository).findById(perfilId);
        verify(permisoRepository, never()).findAllById(any());
        verify(perfilRepository).save(perfilTest);
    }

    @Test
    void reemplazarPermisos_ConPermisosVacios_DeberiaLimpiarPermisos() {
        // Arrange
        Long perfilId = 1L;
        Set<Long> permisoIds = Collections.emptySet();
        
        when(perfilRepository.findById(perfilId)).thenReturn(Optional.of(perfilTest));
        when(perfilRepository.save(any(Perfil.class))).thenReturn(perfilTest);

        // Act
        Perfil resultado = perfilService.reemplazarPermisos(perfilId, permisoIds);

        // Assert
        assertNotNull(resultado);
        verify(perfilRepository).findById(perfilId);
        verify(permisoRepository, never()).findAllById(any());
        verify(perfilRepository).save(perfilTest);
    }
}
