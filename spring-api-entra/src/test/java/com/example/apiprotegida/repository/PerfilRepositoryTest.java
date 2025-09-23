package com.example.apiprotegida.repository;

import com.example.apiprotegida.model.Perfil;
import com.example.apiprotegida.model.Permiso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para PerfilRepository
 */
@DataJpaTest
@ActiveProfiles("test")
class PerfilRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PerfilRepository perfilRepository;

    private Perfil perfilAdmin;
    private Perfil perfilUser;
    private Perfil perfilManager;
    private Permiso permisoLeer;
    private Permiso permisoCrear;

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos antes de cada test
        entityManager.getEntityManager().createQuery("DELETE FROM Perfil").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Permiso").executeUpdate();
        entityManager.flush();
        entityManager.clear();

        // Crear permisos con códigos únicos
        permisoLeer = new Permiso("TEST_USUARIOS_LEER", "Leer Usuarios Test", "Permiso para leer usuarios en test", "USUARIOS", "LEER");
        permisoLeer.setActivo(true);
        entityManager.persistAndFlush(permisoLeer);

        permisoCrear = new Permiso("TEST_USUARIOS_CREAR", "Crear Usuarios Test", "Permiso para crear usuarios en test", "USUARIOS", "CREAR");
        permisoCrear.setActivo(true);
        entityManager.persistAndFlush(permisoCrear);

        // Crear perfiles
        perfilAdmin = new Perfil("Administrador", "Perfil de administrador", "admin-group-id", "Admin Group");
        perfilAdmin.setActivo(true);
        perfilAdmin.addPermiso(permisoLeer);
        perfilAdmin.addPermiso(permisoCrear);
        entityManager.persistAndFlush(perfilAdmin);

        perfilUser = new Perfil("Usuario", "Perfil de usuario básico", "user-group-id", "User Group");
        perfilUser.setActivo(true);
        perfilUser.addPermiso(permisoLeer);
        entityManager.persistAndFlush(perfilUser);

        perfilManager = new Perfil("Manager", "Perfil de manager", "manager-group-id", "Manager Group");
        perfilManager.setActivo(false); // Perfil inactivo
        perfilManager.addPermiso(permisoCrear);
        entityManager.persistAndFlush(perfilManager);

        entityManager.clear();
    }

    // ========== TESTS PARA CONSULTAS BÁSICAS ==========

    @Test
    void findByNombre_ConNombreExistente_DeberiaRetornarPerfil() {
        // Act
        Optional<Perfil> resultado = perfilRepository.findByNombre("Administrador");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Administrador", resultado.get().getNombre());
        assertEquals("admin-group-id", resultado.get().getAzureGroupId());
    }

    @Test
    void findByNombre_ConNombreInexistente_DeberiaRetornarVacio() {
        // Act
        Optional<Perfil> resultado = perfilRepository.findByNombre("PerfilInexistente");

        // Assert
        assertFalse(resultado.isPresent());
    }

    @Test
    void findByAzureGroupId_ConGroupIdExistente_DeberiaRetornarPerfil() {
        // Act
        Optional<Perfil> resultado = perfilRepository.findByAzureGroupId("admin-group-id");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Administrador", resultado.get().getNombre());
        assertEquals("admin-group-id", resultado.get().getAzureGroupId());
    }

    @Test
    void findByAzureGroupId_ConGroupIdInexistente_DeberiaRetornarVacio() {
        // Act
        Optional<Perfil> resultado = perfilRepository.findByAzureGroupId("group-inexistente");

        // Assert
        assertFalse(resultado.isPresent());
    }

    @Test
    void findByAzureGroupName_ConGroupNameExistente_DeberiaRetornarPerfil() {
        // Act
        Optional<Perfil> resultado = perfilRepository.findByAzureGroupName("Admin Group");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Administrador", resultado.get().getNombre());
        assertEquals("Admin Group", resultado.get().getAzureGroupName());
    }

    @Test
    void findByAzureGroupName_ConGroupNameInexistente_DeberiaRetornarVacio() {
        // Act
        Optional<Perfil> resultado = perfilRepository.findByAzureGroupName("Group Inexistente");

        // Assert
        assertFalse(resultado.isPresent());
    }

    @Test
    void findByActivoTrue_DeberiaRetornarSoloPerfilesActivos() {
        // Act
        List<Perfil> resultado = perfilRepository.findByActivoTrue();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(Perfil::getActivo));
        assertTrue(resultado.stream().anyMatch(p -> "Administrador".equals(p.getNombre())));
        assertTrue(resultado.stream().anyMatch(p -> "Usuario".equals(p.getNombre())));
    }

    // ========== TESTS PARA CONSULTAS PERSONALIZADAS ==========

    @Test
    void findByAzureGroupIds_ConGroupIdsValidos_DeberiaRetornarPerfilesActivos() {
        // Arrange
        List<String> azureGroupIds = Arrays.asList("admin-group-id", "user-group-id");

        // Act
        List<Perfil> resultado = perfilRepository.findByAzureGroupIds(azureGroupIds);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(Perfil::getActivo));
        assertTrue(resultado.stream().anyMatch(p -> "admin-group-id".equals(p.getAzureGroupId())));
        assertTrue(resultado.stream().anyMatch(p -> "user-group-id".equals(p.getAzureGroupId())));
    }

    @Test
    void findByAzureGroupIds_ConGroupIdsIncluyendoInactivos_DeberiaRetornarSoloActivos() {
        // Arrange
        List<String> azureGroupIds = Arrays.asList("admin-group-id", "manager-group-id");

        // Act
        List<Perfil> resultado = perfilRepository.findByAzureGroupIds(azureGroupIds);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.stream().allMatch(Perfil::getActivo));
        assertEquals("admin-group-id", resultado.get(0).getAzureGroupId());
    }

    @Test
    void findByAzureGroupIds_ConGroupIdsInexistentes_DeberiaRetornarListaVacia() {
        // Arrange
        List<String> azureGroupIds = Arrays.asList("group-inexistente-1", "group-inexistente-2");

        // Act
        List<Perfil> resultado = perfilRepository.findByAzureGroupIds(azureGroupIds);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void findAllWithPermisos_DeberiaRetornarPerfilesConPermisosCargados() {
        // Act
        List<Perfil> resultado = perfilRepository.findAllWithPermisos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(Perfil::getActivo));
        
        // Verificar que los permisos están cargados (no lazy)
        Perfil perfilAdmin = resultado.stream()
            .filter(p -> "Administrador".equals(p.getNombre()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(perfilAdmin);
        assertNotNull(perfilAdmin.getPermisos());
        assertFalse(perfilAdmin.getPermisos().isEmpty());
        assertEquals(2, perfilAdmin.getPermisos().size());
    }

    @Test
    void findByAzureGroupIdWithPermisos_ConGroupIdValido_DeberiaRetornarPerfilConPermisos() {
        // Act
        Optional<Perfil> resultado = perfilRepository.findByAzureGroupIdWithPermisos("admin-group-id");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Administrador", resultado.get().getNombre());
        assertNotNull(resultado.get().getPermisos());
        assertFalse(resultado.get().getPermisos().isEmpty());
        assertEquals(2, resultado.get().getPermisos().size());
    }

    @Test
    void findByAzureGroupIdWithPermisos_ConGroupIdInactivo_DeberiaRetornarVacio() {
        // Act
        Optional<Perfil> resultado = perfilRepository.findByAzureGroupIdWithPermisos("manager-group-id");

        // Assert
        assertFalse(resultado.isPresent());
    }

    @Test
    void findByAzureGroupIdWithPermisos_ConGroupIdInexistente_DeberiaRetornarVacio() {
        // Act
        Optional<Perfil> resultado = perfilRepository.findByAzureGroupIdWithPermisos("group-inexistente");

        // Assert
        assertFalse(resultado.isPresent());
    }

    // ========== TESTS PARA MÉTODOS DE VERIFICACIÓN ==========

    @Test
    void existsByNombre_ConNombreExistente_DeberiaRetornarTrue() {
        // Act
        boolean resultado = perfilRepository.existsByNombre("Administrador");

        // Assert
        assertTrue(resultado);
    }

    @Test
    void existsByNombre_ConNombreInexistente_DeberiaRetornarFalse() {
        // Act
        boolean resultado = perfilRepository.existsByNombre("PerfilInexistente");

        // Assert
        assertFalse(resultado);
    }

    @Test
    void existsByAzureGroupId_ConGroupIdExistente_DeberiaRetornarTrue() {
        // Act
        boolean resultado = perfilRepository.existsByAzureGroupId("admin-group-id");

        // Assert
        assertTrue(resultado);
    }

    @Test
    void existsByAzureGroupId_ConGroupIdInexistente_DeberiaRetornarFalse() {
        // Act
        boolean resultado = perfilRepository.existsByAzureGroupId("group-inexistente");

        // Assert
        assertFalse(resultado);
    }

    // ========== TESTS ADICIONALES PARA CASOS EDGE ==========

    @Test
    void findByNombre_ConNombreNull_DeberiaRetornarVacio() {
        // Act
        Optional<Perfil> resultado = perfilRepository.findByNombre(null);

        // Assert
        assertFalse(resultado.isPresent());
    }

    @Test
    void findByAzureGroupId_ConGroupIdNull_DeberiaRetornarVacio() {
        // Act
        Optional<Perfil> resultado = perfilRepository.findByAzureGroupId(null);

        // Assert
        assertFalse(resultado.isPresent());
    }

    @Test
    void findByAzureGroupName_ConGroupNameNull_DeberiaRetornarVacio() {
        // Act
        Optional<Perfil> resultado = perfilRepository.findByAzureGroupName(null);

        // Assert
        assertFalse(resultado.isPresent());
    }

    @Test
    void findByAzureGroupIds_ConListaVacia_DeberiaRetornarListaVacia() {
        // Arrange
        List<String> azureGroupIds = Arrays.asList();

        // Act
        List<Perfil> resultado = perfilRepository.findByAzureGroupIds(azureGroupIds);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void findByAzureGroupIds_ConListaNull_DeberiaRetornarListaVacia() {
        // Act
        List<Perfil> resultado = perfilRepository.findByAzureGroupIds(null);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void existsByNombre_ConNombreNull_DeberiaRetornarFalse() {
        // Act
        boolean resultado = perfilRepository.existsByNombre(null);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void existsByAzureGroupId_ConGroupIdNull_DeberiaRetornarFalse() {
        // Act
        boolean resultado = perfilRepository.existsByAzureGroupId(null);

        // Assert
        assertFalse(resultado);
    }

    // ========== TESTS PARA VERIFICAR RELACIONES ==========

    @Test
    void findAllWithPermisos_DeberiaMantenerRelacionesCorrectas() {
        // Act
        List<Perfil> resultado = perfilRepository.findAllWithPermisos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        
        // Verificar que cada perfil tiene sus permisos correctos
        for (Perfil perfil : resultado) {
            assertNotNull(perfil.getPermisos());
            if ("Administrador".equals(perfil.getNombre())) {
                assertEquals(2, perfil.getPermisos().size());
                assertTrue(perfil.getPermisos().stream().anyMatch(p -> "TEST_USUARIOS_LEER".equals(p.getCodigo())));
                assertTrue(perfil.getPermisos().stream().anyMatch(p -> "TEST_USUARIOS_CREAR".equals(p.getCodigo())));
            } else if ("Usuario".equals(perfil.getNombre())) {
                assertEquals(1, perfil.getPermisos().size());
                assertTrue(perfil.getPermisos().stream().anyMatch(p -> "TEST_USUARIOS_LEER".equals(p.getCodigo())));
            }
        }
    }

    @Test
    void findByAzureGroupIdWithPermisos_DeberiaMantenerRelacionesCorrectas() {
        // Act
        Optional<Perfil> resultado = perfilRepository.findByAzureGroupIdWithPermisos("admin-group-id");

        // Assert
        assertTrue(resultado.isPresent());
        Perfil perfil = resultado.get();
        assertEquals("Administrador", perfil.getNombre());
        assertNotNull(perfil.getPermisos());
        assertEquals(2, perfil.getPermisos().size());
        
        // Verificar que los permisos están correctamente asociados
        assertTrue(perfil.getPermisos().stream().anyMatch(p -> "TEST_USUARIOS_LEER".equals(p.getCodigo())));
        assertTrue(perfil.getPermisos().stream().anyMatch(p -> "TEST_USUARIOS_CREAR".equals(p.getCodigo())));
    }
}
