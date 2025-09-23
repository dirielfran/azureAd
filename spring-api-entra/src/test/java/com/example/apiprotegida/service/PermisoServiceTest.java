package com.example.apiprotegida.service;

import com.example.apiprotegida.model.Permiso;
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
 * Tests unitarios para PermisoService
 */
@ExtendWith(MockitoExtension.class)
class PermisoServiceTest {

    @Mock
    private PermisoRepository permisoRepository;

    @InjectMocks
    private PermisoService permisoService;

    private Permiso permisoTest;

    @BeforeEach
    void setUp() {
        // Crear datos de prueba
        permisoTest = new Permiso("USUARIOS_LEER", "Leer Usuarios", "Permiso para leer usuarios", "USUARIOS", "LEER");
        permisoTest.setId(1L);
        permisoTest.setActivo(true);
    }

    // ========== TESTS PARA MÉTODOS DE CONSULTA ==========

    @Test
    void obtenerTodosLosPermisos_DeberiaRetornarListaDePermisos() {
        // Arrange
        List<Permiso> permisosEsperados = Arrays.asList(permisoTest);
        when(permisoRepository.findByActivoTrue()).thenReturn(permisosEsperados);

        // Act
        List<Permiso> resultado = permisoService.obtenerTodosLosPermisos();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("USUARIOS_LEER", resultado.get(0).getCodigo());
        verify(permisoRepository).findByActivoTrue();
    }

    @Test
    void obtenerPermisoPorId_ConIdValido_DeberiaRetornarPermiso() {
        // Arrange
        Long id = 1L;
        when(permisoRepository.findById(id)).thenReturn(Optional.of(permisoTest));

        // Act
        Optional<Permiso> resultado = permisoService.obtenerPermisoPorId(id);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("USUARIOS_LEER", resultado.get().getCodigo());
        verify(permisoRepository).findById(id);
    }

    @Test
    void obtenerPermisoPorId_ConIdInvalido_DeberiaRetornarVacio() {
        // Arrange
        Long id = 999L;
        when(permisoRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<Permiso> resultado = permisoService.obtenerPermisoPorId(id);

        // Assert
        assertFalse(resultado.isPresent());
        verify(permisoRepository).findById(id);
    }

    @Test
    void obtenerPermisoPorCodigo_ConCodigoValido_DeberiaRetornarPermiso() {
        // Arrange
        String codigo = "USUARIOS_LEER";
        when(permisoRepository.findByCodigo(codigo)).thenReturn(Optional.of(permisoTest));

        // Act
        Optional<Permiso> resultado = permisoService.obtenerPermisoPorCodigo(codigo);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("USUARIOS_LEER", resultado.get().getCodigo());
        verify(permisoRepository).findByCodigo(codigo);
    }

    @Test
    void obtenerPermisoPorCodigo_ConCodigoInvalido_DeberiaRetornarVacio() {
        // Arrange
        String codigo = "CODIGO_INEXISTENTE";
        when(permisoRepository.findByCodigo(codigo)).thenReturn(Optional.empty());

        // Act
        Optional<Permiso> resultado = permisoService.obtenerPermisoPorCodigo(codigo);

        // Assert
        assertFalse(resultado.isPresent());
        verify(permisoRepository).findByCodigo(codigo);
    }

    @Test
    void obtenerPermisosPorModulo_ConModuloValido_DeberiaRetornarPermisos() {
        // Arrange
        String modulo = "USUARIOS";
        List<Permiso> permisosEsperados = Arrays.asList(permisoTest);
        when(permisoRepository.findByModuloAndActivoTrue(modulo)).thenReturn(permisosEsperados);

        // Act
        List<Permiso> resultado = permisoService.obtenerPermisosPorModulo(modulo);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("USUARIOS", resultado.get(0).getModulo());
        verify(permisoRepository).findByModuloAndActivoTrue(modulo);
    }

    @Test
    void obtenerPermisosPorAccion_ConAccionValida_DeberiaRetornarPermisos() {
        // Arrange
        String accion = "LEER";
        List<Permiso> permisosEsperados = Arrays.asList(permisoTest);
        when(permisoRepository.findByAccionAndActivoTrue(accion)).thenReturn(permisosEsperados);

        // Act
        List<Permiso> resultado = permisoService.obtenerPermisosPorAccion(accion);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("LEER", resultado.get(0).getAccion());
        verify(permisoRepository).findByAccionAndActivoTrue(accion);
    }

    @Test
    void obtenerPermisosPorModuloYAccion_ConParametrosValidos_DeberiaRetornarPermisos() {
        // Arrange
        String modulo = "USUARIOS";
        String accion = "LEER";
        List<Permiso> permisosEsperados = Arrays.asList(permisoTest);
        when(permisoRepository.findByModuloAndAccionAndActivoTrue(modulo, accion)).thenReturn(permisosEsperados);

        // Act
        List<Permiso> resultado = permisoService.obtenerPermisosPorModuloYAccion(modulo, accion);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("USUARIOS", resultado.get(0).getModulo());
        assertEquals("LEER", resultado.get(0).getAccion());
        verify(permisoRepository).findByModuloAndAccionAndActivoTrue(modulo, accion);
    }

    @Test
    void obtenerPermisosPorCodigos_ConCodigosValidos_DeberiaRetornarPermisos() {
        // Arrange
        Set<String> codigos = Set.of("USUARIOS_LEER", "USUARIOS_CREAR");
        List<Permiso> permisosEsperados = Arrays.asList(permisoTest);
        when(permisoRepository.findByCodigoInAndActivoTrue(codigos)).thenReturn(permisosEsperados);

        // Act
        List<Permiso> resultado = permisoService.obtenerPermisosPorCodigos(codigos);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(permisoRepository).findByCodigoInAndActivoTrue(codigos);
    }

    @Test
    void obtenerModulos_DeberiaRetornarListaDeModulos() {
        // Arrange
        List<String> modulosEsperados = Arrays.asList("USUARIOS", "REPORTES", "CONFIGURACION");
        when(permisoRepository.findDistinctModulos()).thenReturn(modulosEsperados);

        // Act
        List<String> resultado = permisoService.obtenerModulos();

        // Assert
        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        assertTrue(resultado.contains("USUARIOS"));
        verify(permisoRepository).findDistinctModulos();
    }

    @Test
    void obtenerAcciones_DeberiaRetornarListaDeAcciones() {
        // Arrange
        List<String> accionesEsperadas = Arrays.asList("LEER", "CREAR", "ACTUALIZAR", "ELIMINAR");
        when(permisoRepository.findDistinctAcciones()).thenReturn(accionesEsperadas);

        // Act
        List<String> resultado = permisoService.obtenerAcciones();

        // Assert
        assertNotNull(resultado);
        assertEquals(4, resultado.size());
        assertTrue(resultado.contains("LEER"));
        verify(permisoRepository).findDistinctAcciones();
    }

    @Test
    void obtenerPermisosPorPerfilId_ConPerfilIdValido_DeberiaRetornarPermisos() {
        // Arrange
        Long perfilId = 1L;
        List<Permiso> permisosEsperados = Arrays.asList(permisoTest);
        when(permisoRepository.findByPerfilId(perfilId)).thenReturn(permisosEsperados);

        // Act
        List<Permiso> resultado = permisoService.obtenerPermisosPorPerfilId(perfilId);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(permisoRepository).findByPerfilId(perfilId);
    }

    @Test
    void obtenerPermisosPorPerfilIds_ConPerfilIdsValidos_DeberiaRetornarPermisos() {
        // Arrange
        List<Long> perfilIds = Arrays.asList(1L, 2L);
        List<Permiso> permisosEsperados = Arrays.asList(permisoTest);
        when(permisoRepository.findByPerfilIds(perfilIds)).thenReturn(permisosEsperados);

        // Act
        List<Permiso> resultado = permisoService.obtenerPermisosPorPerfilIds(perfilIds);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(permisoRepository).findByPerfilIds(perfilIds);
    }

    // ========== TESTS PARA MÉTODOS DE CREACIÓN ==========

    @Test
    void crearPermiso_ConDatosValidos_DeberiaCrearPermiso() {
        // Arrange
        Permiso nuevoPermiso = new Permiso("USUARIOS_CREAR", "Crear Usuarios", "Permiso para crear usuarios", "USUARIOS", "CREAR");
        when(permisoRepository.existsByCodigo("USUARIOS_CREAR")).thenReturn(false);
        when(permisoRepository.save(any(Permiso.class))).thenReturn(nuevoPermiso);

        // Act
        Permiso resultado = permisoService.crearPermiso(nuevoPermiso);

        // Assert
        assertNotNull(resultado);
        assertEquals("USUARIOS_CREAR", resultado.getCodigo());
        verify(permisoRepository).existsByCodigo("USUARIOS_CREAR");
        verify(permisoRepository).save(nuevoPermiso);
    }

    @Test
    void crearPermiso_ConCodigoDuplicado_DeberiaLanzarExcepcion() {
        // Arrange
        Permiso nuevoPermiso = new Permiso("USUARIOS_LEER", "Leer Usuarios", "Permiso para leer usuarios", "USUARIOS", "LEER");
        when(permisoRepository.existsByCodigo("USUARIOS_LEER")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, 
            () -> permisoService.crearPermiso(nuevoPermiso));
        
        assertEquals("Ya existe un permiso con el código: USUARIOS_LEER", excepcion.getMessage());
        verify(permisoRepository).existsByCodigo("USUARIOS_LEER");
        verify(permisoRepository, never()).save(any(Permiso.class));
    }

    // ========== TESTS PARA MÉTODOS DE ACTUALIZACIÓN ==========

    @Test
    void actualizarPermiso_ConDatosValidos_DeberiaActualizarPermiso() {
        // Arrange
        Long id = 1L;
        Permiso permisoActualizado = new Permiso("USUARIOS_LEER_ACTUALIZADO", "Leer Usuarios Actualizado", "Descripción actualizada", "USUARIOS", "LEER");
        permisoActualizado.setActivo(true);
        
        when(permisoRepository.findById(id)).thenReturn(Optional.of(permisoTest));
        when(permisoRepository.existsByCodigo("USUARIOS_LEER_ACTUALIZADO")).thenReturn(false);
        when(permisoRepository.save(any(Permiso.class))).thenReturn(permisoActualizado);

        // Act
        Permiso resultado = permisoService.actualizarPermiso(id, permisoActualizado);

        // Assert
        assertNotNull(resultado);
        assertEquals("USUARIOS_LEER_ACTUALIZADO", resultado.getCodigo());
        verify(permisoRepository).findById(id);
        verify(permisoRepository).existsByCodigo("USUARIOS_LEER_ACTUALIZADO");
        verify(permisoRepository).save(any(Permiso.class));
    }

    @Test
    void actualizarPermiso_ConIdInexistente_DeberiaLanzarExcepcion() {
        // Arrange
        Long id = 999L;
        Permiso permisoActualizado = new Permiso("USUARIOS_LEER_ACTUALIZADO", "Leer Usuarios Actualizado", "Descripción actualizada", "USUARIOS", "LEER");
        when(permisoRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, 
            () -> permisoService.actualizarPermiso(id, permisoActualizado));
        
        assertEquals("Permiso no encontrado con ID: 999", excepcion.getMessage());
        verify(permisoRepository).findById(id);
        verify(permisoRepository, never()).save(any(Permiso.class));
    }

    @Test
    void actualizarPermiso_ConCodigoDuplicado_DeberiaLanzarExcepcion() {
        // Arrange
        Long id = 1L;
        Permiso permisoActualizado = new Permiso("USUARIOS_CREAR", "Crear Usuarios", "Permiso para crear usuarios", "USUARIOS", "CREAR");
        when(permisoRepository.findById(id)).thenReturn(Optional.of(permisoTest));
        when(permisoRepository.existsByCodigo("USUARIOS_CREAR")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, 
            () -> permisoService.actualizarPermiso(id, permisoActualizado));
        
        assertEquals("Ya existe un permiso con el código: USUARIOS_CREAR", excepcion.getMessage());
        verify(permisoRepository).findById(id);
        verify(permisoRepository).existsByCodigo("USUARIOS_CREAR");
        verify(permisoRepository, never()).save(any(Permiso.class));
    }

    @Test
    void actualizarPermiso_ConMismoCodigo_DeberiaActualizarPermiso() {
        // Arrange
        Long id = 1L;
        Permiso permisoActualizado = new Permiso("USUARIOS_LEER", "Leer Usuarios Actualizado", "Descripción actualizada", "USUARIOS", "LEER");
        permisoActualizado.setActivo(true);
        
        when(permisoRepository.findById(id)).thenReturn(Optional.of(permisoTest));
        when(permisoRepository.save(any(Permiso.class))).thenReturn(permisoActualizado);

        // Act
        Permiso resultado = permisoService.actualizarPermiso(id, permisoActualizado);

        // Assert
        assertNotNull(resultado);
        assertEquals("USUARIOS_LEER", resultado.getCodigo());
        verify(permisoRepository).findById(id);
        verify(permisoRepository, never()).existsByCodigo(any());
        verify(permisoRepository).save(any(Permiso.class));
    }

    // ========== TESTS PARA MÉTODOS DE ELIMINACIÓN ==========

    @Test
    void desactivarPermiso_ConIdValido_DeberiaDesactivarPermiso() {
        // Arrange
        Long id = 1L;
        when(permisoRepository.findById(id)).thenReturn(Optional.of(permisoTest));
        when(permisoRepository.save(any(Permiso.class))).thenReturn(permisoTest);

        // Act
        permisoService.desactivarPermiso(id);

        // Assert
        assertFalse(permisoTest.getActivo());
        verify(permisoRepository).findById(id);
        verify(permisoRepository).save(permisoTest);
    }

    @Test
    void desactivarPermiso_ConIdInexistente_DeberiaLanzarExcepcion() {
        // Arrange
        Long id = 999L;
        when(permisoRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, 
            () -> permisoService.desactivarPermiso(id));
        
        assertEquals("Permiso no encontrado con ID: 999", excepcion.getMessage());
        verify(permisoRepository).findById(id);
        verify(permisoRepository, never()).save(any(Permiso.class));
    }

    @Test
    void eliminarPermiso_ConIdValido_DeberiaEliminarPermiso() {
        // Arrange
        Long id = 1L;
        when(permisoRepository.existsById(id)).thenReturn(true);

        // Act
        permisoService.eliminarPermiso(id);

        // Assert
        verify(permisoRepository).existsById(id);
        verify(permisoRepository).deleteById(id);
    }

    @Test
    void eliminarPermiso_ConIdInexistente_DeberiaLanzarExcepcion() {
        // Arrange
        Long id = 999L;
        when(permisoRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, 
            () -> permisoService.eliminarPermiso(id));
        
        assertEquals("Permiso no encontrado con ID: 999", excepcion.getMessage());
        verify(permisoRepository).existsById(id);
        verify(permisoRepository, never()).deleteById(any());
    }

    // ========== TESTS PARA MÉTODOS DE ESTADÍSTICAS Y UTILIDADES ==========

    @Test
    void obtenerEstadisticasPorModulo_DeberiaRetornarEstadisticas() {
        // Arrange
        List<Object[]> estadisticasEsperadas = Arrays.asList(
            new Object[]{"USUARIOS", 5L},
            new Object[]{"REPORTES", 3L},
            new Object[]{"CONFIGURACION", 2L}
        );
        when(permisoRepository.countPermisosByModulo()).thenReturn(estadisticasEsperadas);

        // Act
        List<Object[]> resultado = permisoService.obtenerEstadisticasPorModulo();

        // Assert
        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        assertEquals("USUARIOS", resultado.get(0)[0]);
        assertEquals(5L, resultado.get(0)[1]);
        verify(permisoRepository).countPermisosByModulo();
    }

    @Test
    void existePermiso_ConCodigoExistente_DeberiaRetornarTrue() {
        // Arrange
        String codigo = "USUARIOS_LEER";
        when(permisoRepository.existsByCodigo(codigo)).thenReturn(true);

        // Act
        boolean resultado = permisoService.existePermiso(codigo);

        // Assert
        assertTrue(resultado);
        verify(permisoRepository).existsByCodigo(codigo);
    }

    @Test
    void existePermiso_ConCodigoInexistente_DeberiaRetornarFalse() {
        // Arrange
        String codigo = "CODIGO_INEXISTENTE";
        when(permisoRepository.existsByCodigo(codigo)).thenReturn(false);

        // Act
        boolean resultado = permisoService.existePermiso(codigo);

        // Assert
        assertFalse(resultado);
        verify(permisoRepository).existsByCodigo(codigo);
    }

    // ========== TESTS ADICIONALES PARA CASOS EDGE ==========

    @Test
    void obtenerPermisosPorModulo_ConModuloInexistente_DeberiaRetornarListaVacia() {
        // Arrange
        String modulo = "MODULO_INEXISTENTE";
        when(permisoRepository.findByModuloAndActivoTrue(modulo)).thenReturn(Collections.emptyList());

        // Act
        List<Permiso> resultado = permisoService.obtenerPermisosPorModulo(modulo);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(permisoRepository).findByModuloAndActivoTrue(modulo);
    }

    @Test
    void obtenerPermisosPorAccion_ConAccionInexistente_DeberiaRetornarListaVacia() {
        // Arrange
        String accion = "ACCION_INEXISTENTE";
        when(permisoRepository.findByAccionAndActivoTrue(accion)).thenReturn(Collections.emptyList());

        // Act
        List<Permiso> resultado = permisoService.obtenerPermisosPorAccion(accion);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(permisoRepository).findByAccionAndActivoTrue(accion);
    }

    @Test
    void obtenerPermisosPorCodigos_ConCodigosVacios_DeberiaRetornarListaVacia() {
        // Arrange
        Set<String> codigos = Collections.emptySet();
        when(permisoRepository.findByCodigoInAndActivoTrue(codigos)).thenReturn(Collections.emptyList());

        // Act
        List<Permiso> resultado = permisoService.obtenerPermisosPorCodigos(codigos);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(permisoRepository).findByCodigoInAndActivoTrue(codigos);
    }

    @Test
    void obtenerPermisosPorPerfilIds_ConListaVacia_DeberiaRetornarListaVacia() {
        // Arrange
        List<Long> perfilIds = Collections.emptyList();
        when(permisoRepository.findByPerfilIds(perfilIds)).thenReturn(Collections.emptyList());

        // Act
        List<Permiso> resultado = permisoService.obtenerPermisosPorPerfilIds(perfilIds);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(permisoRepository).findByPerfilIds(perfilIds);
    }
}
