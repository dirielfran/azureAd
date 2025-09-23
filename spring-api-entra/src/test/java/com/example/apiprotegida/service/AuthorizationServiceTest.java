package com.example.apiprotegida.service;

import com.example.apiprotegida.model.Perfil;
import com.example.apiprotegida.model.Permiso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AuthorizationService
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private PerfilService perfilService;

    @Mock
    private PermisoService permisoService;

    @InjectMocks
    private AuthorizationService authorizationService;

    private Authentication mockAuthentication;
    private Jwt mockJwt;
    private List<Perfil> mockPerfiles;

    @BeforeEach
    void setUp() {
        // Configurar mocks básicos
        mockAuthentication = mock(Authentication.class);
        mockJwt = mock(Jwt.class);
        
        // Crear datos de prueba
        setupTestData();
    }

    private void setupTestData() {
        // Crear permisos de prueba
        Permiso permiso1 = new Permiso("USUARIOS_LEER", "Leer Usuarios", "Permiso para leer usuarios", "USUARIOS", "LEER");
        permiso1.setId(1L);
        permiso1.setActivo(true);

        Permiso permiso2 = new Permiso("USUARIOS_CREAR", "Crear Usuarios", "Permiso para crear usuarios", "USUARIOS", "CREAR");
        permiso2.setId(2L);
        permiso2.setActivo(true);

        Permiso permiso3 = new Permiso("REPORTES_LEER", "Leer Reportes", "Permiso para leer reportes", "REPORTES", "LEER");
        permiso3.setId(3L);
        permiso3.setActivo(false); // Inactivo


        // Crear perfiles de prueba
        Perfil perfilAdmin = new Perfil("Administrador", "Perfil de administrador", "admin-group-id", "Admin Group");
        perfilAdmin.setId(1L);
        perfilAdmin.addPermiso(permiso1);
        perfilAdmin.addPermiso(permiso2);

        Perfil perfilUsuario = new Perfil("Usuario", "Perfil de usuario", "user-group-id", "User Group");
        perfilUsuario.setId(2L);
        perfilUsuario.addPermiso(permiso1);

        Perfil perfilDefault = new Perfil("Usuario por defecto", "Perfil por defecto", "default-user", "Default User");
        perfilDefault.setId(3L);
        perfilDefault.addPermiso(permiso1);

        mockPerfiles = Arrays.asList(perfilAdmin, perfilUsuario, perfilDefault);
    }

    @Test
    void obtenerPermisosUsuario_ConGruposAzureAD_DeberiaRetornarPermisos() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        List<Permiso> resultado = authorizationService.obtenerPermisosUsuario(mockAuthentication);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().anyMatch(p -> "USUARIOS_LEER".equals(p.getCodigo())));
        assertTrue(resultado.stream().anyMatch(p -> "USUARIOS_CREAR".equals(p.getCodigo())));
        verify(perfilService).obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id"));
    }

    @Test
    void obtenerPermisosUsuario_SinGruposAzureAD_DeberiaAsignarPerfilDefault() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Collections.emptyList());
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("default-user")))
                .thenReturn(Arrays.asList(mockPerfiles.get(2)));

        // Act
        List<Permiso> resultado = authorizationService.obtenerPermisosUsuario(mockAuthentication);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("USUARIOS_LEER", resultado.get(0).getCodigo());
        verify(perfilService).obtenerPerfilesPorAzureGroupIds(Arrays.asList("default-user"));
    }

    @Test
    void obtenerPermisosUsuario_GruposSinPerfiles_DeberiaAsignarPerfilDefault() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("unknown-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("unknown-group-id")))
                .thenReturn(Collections.emptyList());
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("default-user")))
                .thenReturn(Arrays.asList(mockPerfiles.get(2)));

        // Act
        List<Permiso> resultado = authorizationService.obtenerPermisosUsuario(mockAuthentication);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("USUARIOS_LEER", resultado.get(0).getCodigo());
        verify(perfilService).obtenerPerfilesPorAzureGroupIds(Arrays.asList("unknown-group-id"));
        verify(perfilService).obtenerPerfilesPorAzureGroupIds(Arrays.asList("default-user"));
    }

    @Test
    void obtenerPermisosUsuario_SoloPermisosActivos_DeberiaFiltrarInactivos() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        List<Permiso> resultado = authorizationService.obtenerPermisosUsuario(mockAuthentication);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.stream().allMatch(Permiso::getActivo));
        verify(perfilService).obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id"));
    }

    @Test
    void obtenerCodigosPermisosUsuario_DeberiaRetornarCodigosUnicos() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        Set<String> resultado = authorizationService.obtenerCodigosPermisosUsuario(mockAuthentication);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.contains("USUARIOS_LEER"));
        assertTrue(resultado.contains("USUARIOS_CREAR"));
    }

    @Test
    void obtenerPerfilesUsuario_ConGruposAzureAD_DeberiaRetornarPerfiles() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        List<Perfil> resultado = authorizationService.obtenerPerfilesUsuario(mockAuthentication);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Administrador", resultado.get(0).getNombre());
    }

    @Test
    void tienePermiso_ConPermisoValido_DeberiaRetornarTrue() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        boolean resultado = authorizationService.tienePermiso(mockAuthentication, "USUARIOS_LEER");

        // Assert
        assertTrue(resultado);
    }

    @Test
    void tienePermiso_ConPermisoInvalido_DeberiaRetornarFalse() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        boolean resultado = authorizationService.tienePermiso(mockAuthentication, "PERMISO_INEXISTENTE");

        // Assert
        assertFalse(resultado);
    }

    @Test
    void tieneAlgunoDeEstosPermisos_ConAlgunoValido_DeberiaRetornarTrue() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        boolean resultado = authorizationService.tieneAlgunoDeEstosPermisos(mockAuthentication, 
                "USUARIOS_LEER", "PERMISO_INEXISTENTE");

        // Assert
        assertTrue(resultado);
    }

    @Test
    void tieneAlgunoDeEstosPermisos_ConNingunoValido_DeberiaRetornarFalse() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        boolean resultado = authorizationService.tieneAlgunoDeEstosPermisos(mockAuthentication, 
                "PERMISO_INEXISTENTE1", "PERMISO_INEXISTENTE2");

        // Assert
        assertFalse(resultado);
    }

    @Test
    void tieneTodosLosPermisos_ConTodosValidos_DeberiaRetornarTrue() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        boolean resultado = authorizationService.tieneTodosLosPermisos(mockAuthentication, 
                "USUARIOS_LEER", "USUARIOS_CREAR");

        // Assert
        assertTrue(resultado);
    }

    @Test
    void tieneTodosLosPermisos_ConAlgunoInvalido_DeberiaRetornarFalse() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        boolean resultado = authorizationService.tieneTodosLosPermisos(mockAuthentication, 
                "USUARIOS_LEER", "PERMISO_INEXISTENTE");

        // Assert
        assertFalse(resultado);
    }

    @Test
    void tienePermisoEnModulo_ConModuloValido_DeberiaRetornarTrue() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        boolean resultado = authorizationService.tienePermisoEnModulo(mockAuthentication, "USUARIOS");

        // Assert
        assertTrue(resultado);
    }

    @Test
    void tienePermisoEnModulo_ConModuloInvalido_DeberiaRetornarFalse() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        boolean resultado = authorizationService.tienePermisoEnModulo(mockAuthentication, "MODULO_INEXISTENTE");

        // Assert
        assertFalse(resultado);
    }

    @Test
    void tienePermisoParaAccion_ConAccionValida_DeberiaRetornarTrue() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        boolean resultado = authorizationService.tienePermisoParaAccion(mockAuthentication, "LEER");

        // Assert
        assertTrue(resultado);
    }

    @Test
    void tienePermisoParaAccion_ConAccionInvalida_DeberiaRetornarFalse() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        boolean resultado = authorizationService.tienePermisoParaAccion(mockAuthentication, "ACCION_INEXISTENTE");

        // Assert
        assertFalse(resultado);
    }

    @Test
    void tienePermisoEnModuloYAccion_ConModuloYAccionValidos_DeberiaRetornarTrue() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        boolean resultado = authorizationService.tienePermisoEnModuloYAccion(mockAuthentication, "USUARIOS", "LEER");

        // Assert
        assertTrue(resultado);
    }

    @Test
    void tienePermisoEnModuloYAccion_ConModuloOAccionInvalidos_DeberiaRetornarFalse() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        boolean resultado = authorizationService.tienePermisoEnModuloYAccion(mockAuthentication, "USUARIOS", "ACCION_INEXISTENTE");

        // Assert
        assertFalse(resultado);
    }

    @Test
    void obtenerInformacionCompleteUsuario_DeberiaRetornarInformacionCompleta() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(mockJwt.getClaimAsString("email")).thenReturn("admin@test.com");
        when(mockJwt.getClaimAsString("name")).thenReturn("Admin User");
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        Map<String, Object> resultado = authorizationService.obtenerInformacionCompleteUsuario(mockAuthentication);

        // Assert
        assertNotNull(resultado);
        assertEquals("admin@test.com", resultado.get("email"));
        assertEquals("Admin User", resultado.get("nombre"));
        assertEquals(Arrays.asList("admin-group-id"), resultado.get("grupos"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> perfiles = (List<Map<String, Object>>) resultado.get("perfiles");
        assertNotNull(perfiles);
        assertEquals(1, perfiles.size());
        assertEquals("Administrador", perfiles.get(0).get("nombre"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> permisos = (List<Map<String, Object>>) resultado.get("permisos");
        assertNotNull(permisos);
        assertEquals(2, permisos.size());
        
        @SuppressWarnings("unchecked")
        Set<String> codigosPermisos = (Set<String>) resultado.get("codigosPermisos");
        assertNotNull(codigosPermisos);
        assertEquals(2, codigosPermisos.size());
        assertTrue(codigosPermisos.contains("USUARIOS_LEER"));
        assertTrue(codigosPermisos.contains("USUARIOS_CREAR"));
    }

    @Test
    void extraerGruposAzureAD_ConJwtValido_DeberiaExtraerGrupos() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("group1", "group2"));

        // Act
        authorizationService.obtenerPermisosUsuario(mockAuthentication);

        // Assert
        verify(perfilService).obtenerPerfilesPorAzureGroupIds(Arrays.asList("group1", "group2"));
    }

    @Test
    void extraerGruposAzureAD_ConAuthorities_DeberiaExtraerGruposDeAuthorities() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(null);
        when(mockAuthentication.getAuthorities()).thenReturn(Collections.emptyList());
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("default-user")))
                .thenReturn(Arrays.asList(mockPerfiles.get(2)));

        // Act
        List<Permiso> resultado = authorizationService.obtenerPermisosUsuario(mockAuthentication);

        // Assert
        assertNotNull(resultado);
        verify(perfilService).obtenerPerfilesPorAzureGroupIds(Arrays.asList("default-user"));
    }

    @Test
    void extraerGruposAzureAD_SinGrupos_DeberiaRetornarListaVacia() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(null);
        when(mockAuthentication.getAuthorities()).thenReturn(Collections.emptyList());
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("default-user")))
                .thenReturn(Arrays.asList(mockPerfiles.get(2)));

        // Act
        List<Permiso> resultado = authorizationService.obtenerPermisosUsuario(mockAuthentication);

        // Assert
        assertNotNull(resultado);
        verify(perfilService).obtenerPerfilesPorAzureGroupIds(Arrays.asList("default-user"));
    }

    @Test
    void obtenerEmailUsuario_ConEmailEnClaim_DeberiaRetornarEmail() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        Map<String, Object> resultado = authorizationService.obtenerInformacionCompleteUsuario(mockAuthentication);

        // Assert
        assertEquals("test@example.com", resultado.get("email"));
    }

    @Test
    void obtenerEmailUsuario_ConPreferredUsername_DeberiaRetornarPreferredUsername() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsString("email")).thenReturn(null);
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("test@example.com");
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        Map<String, Object> resultado = authorizationService.obtenerInformacionCompleteUsuario(mockAuthentication);

        // Assert
        assertEquals("test@example.com", resultado.get("email"));
    }

    @Test
    void obtenerNombreUsuario_ConNameEnClaim_DeberiaRetornarName() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getClaimAsString("name")).thenReturn("John Doe");
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        Map<String, Object> resultado = authorizationService.obtenerInformacionCompleteUsuario(mockAuthentication);

        // Assert
        assertEquals("John Doe", resultado.get("nombre"));
    }

    @Test
    void obtenerNombreUsuario_ConGivenNameYFamilyName_DeberiaConcatenarNombres() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getClaimAsString("name")).thenReturn(null);
        when(mockJwt.getClaimAsString("given_name")).thenReturn("John");
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        Map<String, Object> resultado = authorizationService.obtenerInformacionCompleteUsuario(mockAuthentication);

        // Assert
        assertEquals("John", resultado.get("nombre"));
    }

    @Test
    void obtenerNombreUsuario_SinNombres_DeberiaRetornarUsuario() {
        // Arrange
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getClaimAsString("name")).thenReturn(null);
        when(mockJwt.getClaimAsString("given_name")).thenReturn(null);
        when(mockJwt.getClaimAsString("family_name")).thenReturn(null);
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(perfilService.obtenerPerfilesPorAzureGroupIds(Arrays.asList("admin-group-id")))
                .thenReturn(Arrays.asList(mockPerfiles.get(0)));

        // Act
        Map<String, Object> resultado = authorizationService.obtenerInformacionCompleteUsuario(mockAuthentication);

        // Assert
        assertEquals("Usuario", resultado.get("nombre"));
    }
}
