package com.example.apiprotegida.config;

import com.example.apiprotegida.model.Perfil;
import com.example.apiprotegida.model.Permiso;
import com.example.apiprotegida.service.PerfilService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AzureAdGroupsJwtConverter
 * 
 * Este convertidor se encarga de extraer grupos de Azure AD del JWT
 * y convertirlos en authorities de Spring Security basado en perfiles de la base de datos
 */
@ExtendWith(MockitoExtension.class)
class AzureAdGroupsJwtConverterTest {

    @Mock
    private PerfilService perfilService;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private AzureAdGroupsJwtConverter converter;

    private Perfil mockPerfil;
    private Permiso mockPermiso1;
    private Permiso mockPermiso2;
    private Permiso mockPermisoInactivo;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // Crear permisos de prueba
        mockPermiso1 = new Permiso("USUARIOS_LEER", "Leer Usuarios", "Permiso para leer usuarios", "USUARIOS", "LEER");
        mockPermiso1.setId(1L);
        mockPermiso1.setActivo(true);

        mockPermiso2 = new Permiso("USUARIOS_CREAR", "Crear Usuarios", "Permiso para crear usuarios", "USUARIOS", "CREAR");
        mockPermiso2.setId(2L);
        mockPermiso2.setActivo(true);

        mockPermisoInactivo = new Permiso("USUARIOS_ELIMINAR", "Eliminar Usuarios", "Permiso para eliminar usuarios", "USUARIOS", "ELIMINAR");
        mockPermisoInactivo.setId(3L);
        mockPermisoInactivo.setActivo(false);

        // Crear perfil de prueba
        mockPerfil = new Perfil("Administrador", "Perfil de administrador", "admin-group-id", "Admin Group");
        mockPerfil.setId(1L);
        mockPerfil.addPermiso(mockPermiso1);
        mockPerfil.addPermiso(mockPermiso2);
        mockPerfil.addPermiso(mockPermisoInactivo);
    }

    @Test
    void convert_ConGruposValidos_DeberiaRetornarAuthoritiesCompletas() {
        // Arrange
        List<String> grupos = Arrays.asList("admin-group-id", "user-group-id");
        when(jwt.getClaimAsStringList("groups")).thenReturn(grupos);
        when(jwt.getClaimAsString("email")).thenReturn("admin@test.com");
        
        when(perfilService.obtenerPerfilPorAzureGroupIdConPermisos("admin-group-id"))
                .thenReturn(Optional.of(mockPerfil));
        when(perfilService.obtenerPerfilPorAzureGroupIdConPermisos("user-group-id"))
                .thenReturn(Optional.empty());

        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Assert
        assertNotNull(authorities);
        assertFalse(authorities.isEmpty());
        
        // Verificar que se agregaron los grupos como authorities
        assertTrue(authorities.stream().anyMatch(auth -> "GROUP_admin-group-id".equals(auth.getAuthority())));
        assertTrue(authorities.stream().anyMatch(auth -> "GROUP_user-group-id".equals(auth.getAuthority())));
        
        // Verificar que se agregó el rol del perfil
        assertTrue(authorities.stream().anyMatch(auth -> "ROLE_ADMIN_GROUP".equals(auth.getAuthority())));
        
        // Verificar que se agregaron los permisos activos (usando getNombre(), no getCodigo())
        assertTrue(authorities.stream().anyMatch(auth -> "Leer Usuarios".equals(auth.getAuthority())));
        assertTrue(authorities.stream().anyMatch(auth -> "Crear Usuarios".equals(auth.getAuthority())));
        
        // Verificar que NO se agregó el permiso inactivo
        assertFalse(authorities.stream().anyMatch(auth -> "Eliminar Usuarios".equals(auth.getAuthority())));
        
        verify(perfilService, times(2)).obtenerPerfilPorAzureGroupIdConPermisos(anyString());
    }

    @Test
    void convert_ConGruposEnClaimRoles_DeberiaExtraerGruposCorrectamente() {
        // Arrange
        when(jwt.getClaimAsStringList("groups")).thenReturn(null);
        when(jwt.getClaimAsStringList("roles")).thenReturn(Arrays.asList("role-group-id"));
        when(jwt.getClaimAsString("email")).thenReturn("user@test.com");
        
        when(perfilService.obtenerPerfilPorAzureGroupIdConPermisos("role-group-id"))
                .thenReturn(Optional.of(mockPerfil));

        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Assert
        assertNotNull(authorities);
        assertTrue(authorities.stream().anyMatch(auth -> "GROUP_role-group-id".equals(auth.getAuthority())));
        verify(perfilService).obtenerPerfilPorAzureGroupIdConPermisos("role-group-id");
    }

    @Test
    void convert_ConGruposYRoles_DeberiaCombinarAmbosClaims() {
        // Arrange
        when(jwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("group1"));
        when(jwt.getClaimAsStringList("roles")).thenReturn(Arrays.asList("role1"));
        when(jwt.getClaimAsString("email")).thenReturn("user@test.com");
        
        when(perfilService.obtenerPerfilPorAzureGroupIdConPermisos(anyString()))
                .thenReturn(Optional.empty());

        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Assert
        assertNotNull(authorities);
        assertTrue(authorities.stream().anyMatch(auth -> "GROUP_group1".equals(auth.getAuthority())));
        assertTrue(authorities.stream().anyMatch(auth -> "GROUP_role1".equals(auth.getAuthority())));
        verify(perfilService, times(2)).obtenerPerfilPorAzureGroupIdConPermisos(anyString());
    }

    @Test
    void convert_SinGrupos_DeberiaRetornarListaVacia() {
        // Arrange
        when(jwt.getClaimAsStringList("groups")).thenReturn(null);
        when(jwt.getClaimAsStringList("roles")).thenReturn(null);
        when(jwt.getClaimAsString("email")).thenReturn("user@test.com");

        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Assert
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
        verify(perfilService, never()).obtenerPerfilPorAzureGroupIdConPermisos(anyString());
    }

    @Test
    void convert_ConGrupoSinPerfil_DeberiaLogearErrorYContinuar() {
        // Arrange
        List<String> grupos = Arrays.asList("unknown-group-id");
        when(jwt.getClaimAsStringList("groups")).thenReturn(grupos);
        when(jwt.getClaimAsString("email")).thenReturn("user@test.com");
        
        when(perfilService.obtenerPerfilPorAzureGroupIdConPermisos("unknown-group-id"))
                .thenReturn(Optional.empty());

        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Assert
        assertNotNull(authorities);
        // Solo debe tener el authority del grupo, sin rol ni permisos
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(auth -> "GROUP_unknown-group-id".equals(auth.getAuthority())));
        verify(perfilService).obtenerPerfilPorAzureGroupIdConPermisos("unknown-group-id");
    }

    @Test
    void convert_ConErrorEnServicio_DeberiaManejarExcepcionYContinuar() {
        // Arrange
        List<String> grupos = Arrays.asList("error-group-id");
        when(jwt.getClaimAsStringList("groups")).thenReturn(grupos);
        when(jwt.getClaimAsString("email")).thenReturn("user@test.com");
        
        when(perfilService.obtenerPerfilPorAzureGroupIdConPermisos("error-group-id"))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Assert
        assertNotNull(authorities);
        // Debe continuar y agregar al menos el authority del grupo
        assertTrue(authorities.stream().anyMatch(auth -> "GROUP_error-group-id".equals(auth.getAuthority())));
        verify(perfilService).obtenerPerfilPorAzureGroupIdConPermisos("error-group-id");
    }

    @Test
    void convert_ConGrupoVacio_DeberiaManejarListaVacia() {
        // Arrange
        when(jwt.getClaimAsStringList("groups")).thenReturn(Collections.emptyList());
        when(jwt.getClaimAsStringList("roles")).thenReturn(null);
        when(jwt.getClaimAsString("email")).thenReturn("user@test.com");

        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Assert
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
        verify(perfilService, never()).obtenerPerfilPorAzureGroupIdConPermisos(anyString());
    }

    @Test
    void convert_ConNombrePerfilConEspacios_DeberiaFormatearRolCorrectamente() {
        // Arrange
        Perfil perfilConEspacios = new Perfil("Usuario Avanzado", "Perfil con espacios", "user-advanced-id", "User Advanced Group");
        perfilConEspacios.setId(2L);
        
        when(jwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("user-advanced-id"));
        when(jwt.getClaimAsString("email")).thenReturn("user@test.com");
        
        when(perfilService.obtenerPerfilPorAzureGroupIdConPermisos("user-advanced-id"))
                .thenReturn(Optional.of(perfilConEspacios));

        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Assert
        assertNotNull(authorities);
        assertTrue(authorities.stream().anyMatch(auth -> "ROLE_USER_ADVANCED_GROUP".equals(auth.getAuthority())));
        verify(perfilService).obtenerPerfilPorAzureGroupIdConPermisos("user-advanced-id");
    }

    @Test
    void convert_ConPermisosInactivos_DeberiaFiltrarPermisosInactivos() {
        // Arrange
        when(jwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("admin-group-id"));
        when(jwt.getClaimAsString("email")).thenReturn("admin@test.com");
        
        when(perfilService.obtenerPerfilPorAzureGroupIdConPermisos("admin-group-id"))
                .thenReturn(Optional.of(mockPerfil));

        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Assert
        assertNotNull(authorities);
        
        // Verificar permisos activos (usando getNombre())
        assertTrue(authorities.stream().anyMatch(auth -> "Leer Usuarios".equals(auth.getAuthority())));
        assertTrue(authorities.stream().anyMatch(auth -> "Crear Usuarios".equals(auth.getAuthority())));
        
        // Verificar que NO se incluyó el permiso inactivo
        assertFalse(authorities.stream().anyMatch(auth -> "Eliminar Usuarios".equals(auth.getAuthority())));
    }

    @Test
    void convert_ConJwtNull_DeberiaLanzarExcepcion() {
        // Act & Assert
        // El método convert tiene @NonNull en el parámetro, pero permitimos null para testing
        assertThrows(Exception.class, () -> converter.convert(null));
    }

    // Tests para métodos estáticos utilitarios

    @Test
    void hasRole_ConRolExistente_DeberiaRetornarTrue() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
        );

        // Act & Assert
        assertTrue(AzureAdGroupsJwtConverter.hasRole(authorities, "admin"));
        assertTrue(AzureAdGroupsJwtConverter.hasRole(authorities, "ADMIN"));
        assertTrue(AzureAdGroupsJwtConverter.hasRole(authorities, "user"));
    }

    @Test
    void hasRole_ConRolInexistente_DeberiaRetornarFalse() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("PERMISO_LEER")
        );

        // Act & Assert
        assertFalse(AzureAdGroupsJwtConverter.hasRole(authorities, "user"));
        assertFalse(AzureAdGroupsJwtConverter.hasRole(authorities, "guest"));
    }

    @Test
    void hasRole_ConAuthoritiesVacias_DeberiaRetornarFalse() {
        // Arrange
        Collection<GrantedAuthority> authorities = Collections.emptyList();

        // Act & Assert
        assertFalse(AzureAdGroupsJwtConverter.hasRole(authorities, "admin"));
    }

    @Test
    void hasRole_ConAuthoritiesNull_DeberiaLanzarExcepcion() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> 
                AzureAdGroupsJwtConverter.hasRole(null, "admin"));
    }

    @Test
    void extractAzureGroupIds_ConGruposValidos_DeberiaExtraerIdsCorrectamente() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("GROUP_group-123"),
                new SimpleGrantedAuthority("GROUP_group-456"),
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("PERMISO_LEER")
        );

        // Act
        List<String> groupIds = AzureAdGroupsJwtConverter.extractAzureGroupIds(authorities);

        // Assert
        assertNotNull(groupIds);
        assertEquals(2, groupIds.size());
        assertTrue(groupIds.contains("group-123"));
        assertTrue(groupIds.contains("group-456"));
    }

    @Test
    void extractAzureGroupIds_SinGrupos_DeberiaRetornarListaVacia() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("PERMISO_LEER")
        );

        // Act
        List<String> groupIds = AzureAdGroupsJwtConverter.extractAzureGroupIds(authorities);

        // Assert
        assertNotNull(groupIds);
        assertTrue(groupIds.isEmpty());
    }

    @Test
    void extractAzureGroupIds_ConAuthoritiesVacias_DeberiaRetornarListaVacia() {
        // Arrange
        Collection<GrantedAuthority> authorities = Collections.emptyList();

        // Act
        List<String> groupIds = AzureAdGroupsJwtConverter.extractAzureGroupIds(authorities);

        // Assert
        assertNotNull(groupIds);
        assertTrue(groupIds.isEmpty());
    }

    @Test
    void extractAzureGroupIds_ConAuthoritiesNull_DeberiaLanzarExcepcion() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> 
                AzureAdGroupsJwtConverter.extractAzureGroupIds(null));
    }

    // Tests para verificar el comportamiento con diferentes tipos de claims

    @Test
    void convert_ConEmailEnPreferredUsername_DeberiaExtraerEmailCorrectamente() {
        // Arrange
        when(jwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("test-group"));
        when(jwt.getClaimAsString("email")).thenReturn(null);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("user@example.com");
        
        when(perfilService.obtenerPerfilPorAzureGroupIdConPermisos("test-group"))
                .thenReturn(Optional.empty());

        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Assert
        assertNotNull(authorities);
        assertTrue(authorities.stream().anyMatch(auth -> "GROUP_test-group".equals(auth.getAuthority())));
    }

    @Test
    void convert_ConEmailEnUpn_DeberiaExtraerEmailCorrectamente() {
        // Arrange
        when(jwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("test-group"));
        when(jwt.getClaimAsString("email")).thenReturn(null);
        when(jwt.getClaimAsString("preferred_username")).thenReturn(null);
        when(jwt.getClaimAsString("upn")).thenReturn("user@example.com");
        
        when(perfilService.obtenerPerfilPorAzureGroupIdConPermisos("test-group"))
                .thenReturn(Optional.empty());

        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Assert
        assertNotNull(authorities);
        assertTrue(authorities.stream().anyMatch(auth -> "GROUP_test-group".equals(auth.getAuthority())));
    }

    @Test
    void convert_ConEmailNull_DeberiaManejarEmailVacio() {
        // Arrange
        when(jwt.getClaimAsStringList("groups")).thenReturn(Arrays.asList("test-group"));
        when(jwt.getClaimAsString("email")).thenReturn(null);
        when(jwt.getClaimAsString("preferred_username")).thenReturn(null);
        
        when(perfilService.obtenerPerfilPorAzureGroupIdConPermisos("test-group"))
                .thenReturn(Optional.empty());

        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Assert
        assertNotNull(authorities);
        assertTrue(authorities.stream().anyMatch(auth -> "GROUP_test-group".equals(auth.getAuthority())));
    }

    @Test
    void convert_ConMultipleGruposYPerfiles_DeberiaProcesarTodos() {
        // Arrange
        Perfil perfil2 = new Perfil("Editor", "Perfil de editor", "editor-group-id", "Editor Group");
        perfil2.setId(2L);
        perfil2.addPermiso(mockPermiso1);
        
        List<String> grupos = Arrays.asList("admin-group-id", "editor-group-id", "unknown-group");
        when(jwt.getClaimAsStringList("groups")).thenReturn(grupos);
        when(jwt.getClaimAsString("email")).thenReturn("multi@test.com");
        
        when(perfilService.obtenerPerfilPorAzureGroupIdConPermisos("admin-group-id"))
                .thenReturn(Optional.of(mockPerfil));
        when(perfilService.obtenerPerfilPorAzureGroupIdConPermisos("editor-group-id"))
                .thenReturn(Optional.of(perfil2));
        when(perfilService.obtenerPerfilPorAzureGroupIdConPermisos("unknown-group"))
                .thenReturn(Optional.empty());

        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Assert
        assertNotNull(authorities);
        
        // Verificar grupos
        assertTrue(authorities.stream().anyMatch(auth -> "GROUP_admin-group-id".equals(auth.getAuthority())));
        assertTrue(authorities.stream().anyMatch(auth -> "GROUP_editor-group-id".equals(auth.getAuthority())));
        assertTrue(authorities.stream().anyMatch(auth -> "GROUP_unknown-group".equals(auth.getAuthority())));
        
        // Verificar roles
        assertTrue(authorities.stream().anyMatch(auth -> "ROLE_ADMIN_GROUP".equals(auth.getAuthority())));
        assertTrue(authorities.stream().anyMatch(auth -> "ROLE_EDITOR_GROUP".equals(auth.getAuthority())));
        
        // Verificar permisos (de ambos perfiles) - usando getNombre()
        assertTrue(authorities.stream().anyMatch(auth -> "Leer Usuarios".equals(auth.getAuthority())));
        assertTrue(authorities.stream().anyMatch(auth -> "Crear Usuarios".equals(auth.getAuthority())));
        
        verify(perfilService, times(3)).obtenerPerfilPorAzureGroupIdConPermisos(anyString());
    }
}
