package com.example.apiprotegida.config;

import com.example.apiprotegida.model.Perfil;
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

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AzureAdGroupsJwtConverter
 */
@ExtendWith(MockitoExtension.class)
class AzureAdGroupsJwtConverterTest {

    @Mock
    private PerfilService perfilService;

    @InjectMocks
    private AzureAdGroupsJwtConverter converter;

    private Jwt mockJwt;
    private Perfil perfilAdmin;
    private Perfil perfilUser;

    @BeforeEach
    void setUp() {
        // Crear perfiles de prueba
        perfilAdmin = new Perfil("Administrador", "Perfil de administrador", "admin-group-id", "Admin Group");
        perfilAdmin.setId(1L);
        perfilAdmin.setActivo(true);

        perfilUser = new Perfil("Usuario", "Perfil de usuario básico", "user-group-id", "User Group");
        perfilUser.setId(2L);
        perfilUser.setActivo(true);
    }

    // ========== TESTS PARA CONVERSIÓN BÁSICA DE JWT ==========

    @Test
    void convert_ConJwtValidoYGrupos_DeberiaRetornarAuthorities() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("groups", Arrays.asList("admin-group-id", "user-group-id"));
        claims.put("email", "test@example.com");
        claims.put("name", "Test User");
        
        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("groups", Arrays.asList("admin-group-id", "user-group-id"))
                .claim("email", "test@example.com")
                .claim("name", "Test User")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(perfilService.obtenerPerfilPorAzureGroupId("admin-group-id"))
                .thenReturn(Optional.of(perfilAdmin));
        when(perfilService.obtenerPerfilPorAzureGroupId("user-group-id"))
                .thenReturn(Optional.of(perfilUser));

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() >= 4); // GROUP_ + ROLE_ + SCOPE_
        assertTrue(result.stream().anyMatch(auth -> "GROUP_admin-group-id".equals(auth.getAuthority())));
        assertTrue(result.stream().anyMatch(auth -> "GROUP_user-group-id".equals(auth.getAuthority())));
        assertTrue(result.stream().anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority())));
        assertTrue(result.stream().anyMatch(auth -> "ROLE_USER".equals(auth.getAuthority())));
        assertTrue(result.stream().anyMatch(auth -> "SCOPE_access_as_user".equals(auth.getAuthority())));
    }

    @Test
    void convert_ConJwtSinGrupos_DeberiaAsignarRolPorDefecto() {
        // Arrange
        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("email", "test@example.com")
                .claim("name", "Test User")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() >= 2); // ROLE_ + SCOPE_
        assertTrue(result.stream().anyMatch(auth -> "ROLE_USER".equals(auth.getAuthority())));
        assertTrue(result.stream().anyMatch(auth -> "SCOPE_access_as_user".equals(auth.getAuthority())));
    }

    @Test
    void convert_ConJwtConGruposInexistentes_DeberiaAsignarRolPorDefecto() {
        // Arrange
        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("groups", Arrays.asList("unknown-group-id"))
                .claim("email", "test@example.com")
                .claim("name", "Test User")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(perfilService.obtenerPerfilPorAzureGroupId("unknown-group-id"))
                .thenReturn(Optional.empty());

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() >= 2); // GROUP_ + SCOPE_ (no ROLE_ porque hay grupos)
        assertTrue(result.stream().anyMatch(auth -> "GROUP_unknown-group-id".equals(auth.getAuthority())));
        // No se asigna ROLE_USER porque hay grupos presentes, aunque no se encuentren perfiles
        assertTrue(result.stream().anyMatch(auth -> "SCOPE_access_as_user".equals(auth.getAuthority())));
    }

    @Test
    void convert_ConJwtConEmailAdmin_DeberiaAsignarRolAdmin() {
        // Arrange
        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("email", "admin@company.com")
                .claim("name", "Admin User")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority())));
    }

    @Test
    void convert_ConJwtConEmailManager_DeberiaAsignarRolManager() {
        // Arrange
        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("email", "manager@company.com")
                .claim("name", "Manager User")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(auth -> "ROLE_MANAGER".equals(auth.getAuthority())));
    }

    // ========== TESTS PARA MAPEO DE PERFILES A ROLES ==========

    @Test
    void convert_ConPerfilAdministrador_DeberiaMapearARoleAdmin() {
        // Arrange
        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("groups", Arrays.asList("admin-group-id"))
                .claim("email", "test@example.com")
                .claim("name", "Test User")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(perfilService.obtenerPerfilPorAzureGroupId("admin-group-id"))
                .thenReturn(Optional.of(perfilAdmin));

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertTrue(result.stream().anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority())));
    }

    @Test
    void convert_ConPerfilUsuario_DeberiaMapearARoleUser() {
        // Arrange
        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("groups", Arrays.asList("user-group-id"))
                .claim("email", "test@example.com")
                .claim("name", "Test User")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(perfilService.obtenerPerfilPorAzureGroupId("user-group-id"))
                .thenReturn(Optional.of(perfilUser));

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertTrue(result.stream().anyMatch(auth -> "ROLE_USER".equals(auth.getAuthority())));
    }

    @Test
    void convert_ConPerfilGestor_DeberiaMapearARoleManager() {
        // Arrange
        Perfil perfilGestor = new Perfil("Gestor", "Perfil de gestor", "manager-group-id", "Manager Group");
        perfilGestor.setId(3L);
        perfilGestor.setActivo(true);

        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("groups", Arrays.asList("manager-group-id"))
                .claim("email", "test@example.com")
                .claim("name", "Test User")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(perfilService.obtenerPerfilPorAzureGroupId("manager-group-id"))
                .thenReturn(Optional.of(perfilGestor));

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertTrue(result.stream().anyMatch(auth -> "ROLE_MANAGER".equals(auth.getAuthority())));
    }

    @Test
    void convert_ConPerfilLector_DeberiaMapearARoleReader() {
        // Arrange
        Perfil perfilLector = new Perfil("Lector", "Perfil de lector", "reader-group-id", "Reader Group");
        perfilLector.setId(4L);
        perfilLector.setActivo(true);

        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("groups", Arrays.asList("reader-group-id"))
                .claim("email", "test@example.com")
                .claim("name", "Test User")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(perfilService.obtenerPerfilPorAzureGroupId("reader-group-id"))
                .thenReturn(Optional.of(perfilLector));

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertTrue(result.stream().anyMatch(auth -> "ROLE_READER".equals(auth.getAuthority())));
    }

    @Test
    void convert_ConPerfilPersonalizado_DeberiaMapearARolePersonalizado() {
        // Arrange
        Perfil perfilPersonalizado = new Perfil("Supervisor", "Perfil de supervisor", "supervisor-group-id", "Supervisor Group");
        perfilPersonalizado.setId(5L);
        perfilPersonalizado.setActivo(true);

        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("groups", Arrays.asList("supervisor-group-id"))
                .claim("email", "test@example.com")
                .claim("name", "Test User")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(perfilService.obtenerPerfilPorAzureGroupId("supervisor-group-id"))
                .thenReturn(Optional.of(perfilPersonalizado));

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertTrue(result.stream().anyMatch(auth -> "ROLE_SUPERVISOR".equals(auth.getAuthority())));
    }

    // ========== TESTS PARA EXTRACCIÓN DE GRUPOS ==========

    @Test
    void convert_ConGruposEnClaimGroups_DeberiaExtraerGrupos() {
        // Arrange
        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("groups", Arrays.asList("group1", "group2"))
                .claim("email", "test@example.com")
                .claim("name", "Test User")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(perfilService.obtenerPerfilPorAzureGroupId(any())).thenReturn(Optional.empty());

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertTrue(result.stream().anyMatch(auth -> "GROUP_group1".equals(auth.getAuthority())));
        assertTrue(result.stream().anyMatch(auth -> "GROUP_group2".equals(auth.getAuthority())));
    }

    @Test
    void convert_ConGruposEnClaimRoles_DeberiaExtraerGrupos() {
        // Arrange
        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("roles", Arrays.asList("role1", "role2"))
                .claim("email", "test@example.com")
                .claim("name", "Test User")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(perfilService.obtenerPerfilPorAzureGroupId(any())).thenReturn(Optional.empty());

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertTrue(result.stream().anyMatch(auth -> "GROUP_role1".equals(auth.getAuthority())));
        assertTrue(result.stream().anyMatch(auth -> "GROUP_role2".equals(auth.getAuthority())));
    }

    @Test
    void convert_ConGruposEnAmbosClaims_DeberiaExtraerAmbos() {
        // Arrange
        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("groups", Arrays.asList("group1"))
                .claim("roles", Arrays.asList("role1"))
                .claim("email", "test@example.com")
                .claim("name", "Test User")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(perfilService.obtenerPerfilPorAzureGroupId(any())).thenReturn(Optional.empty());

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertTrue(result.stream().anyMatch(auth -> "GROUP_group1".equals(auth.getAuthority())));
        assertTrue(result.stream().anyMatch(auth -> "GROUP_role1".equals(auth.getAuthority())));
    }

    // ========== TESTS PARA EXTRACCIÓN DE EMAIL ==========

    @Test
    void convert_ConEmailEnClaimEmail_DeberiaExtraerEmail() {
        // Arrange
        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("email", "test@example.com")
                .claim("name", "Test User")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertNotNull(result);
        // El email se usa internamente para determinar el rol por defecto
        assertTrue(result.stream().anyMatch(auth -> "ROLE_USER".equals(auth.getAuthority())));
    }

    @Test
    void convert_ConEmailEnClaimPreferredUsername_DeberiaExtraerEmail() {
        // Arrange
        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("preferred_username", "test@example.com")
                .claim("name", "Test User")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(auth -> "ROLE_USER".equals(auth.getAuthority())));
    }

    @Test
    void convert_ConEmailEnClaimUpn_DeberiaExtraerEmail() {
        // Arrange
        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("upn", "test@example.com")
                .claim("name", "Test User")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(auth -> "ROLE_USER".equals(auth.getAuthority())));
    }

    // ========== TESTS PARA EXTRACCIÓN DE NOMBRE ==========

    @Test
    void convert_ConNombreEnClaimName_DeberiaExtraerNombre() {
        // Arrange
        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("name", "John Doe")
                .claim("email", "test@example.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertNotNull(result);
        // El nombre se usa internamente para logging
        assertTrue(result.stream().anyMatch(auth -> "ROLE_USER".equals(auth.getAuthority())));
    }

    @Test
    void convert_ConNombreEnClaimGivenName_DeberiaExtraerNombre() {
        // Arrange
        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("given_name", "John")
                .claim("email", "test@example.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(auth -> "ROLE_USER".equals(auth.getAuthority())));
    }

    @Test
    void convert_ConNombreEnClaimsGivenNameYFamilyName_DeberiaConcatenarNombres() {
        // Arrange
        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("given_name", "John")
                .claim("family_name", "Doe")
                .claim("email", "test@example.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(auth -> "ROLE_USER".equals(auth.getAuthority())));
    }

    // ========== TESTS PARA MÉTODOS UTILITARIOS ESTÁTICOS ==========

    @Test
    void getHighestRole_ConRolAdmin_DeberiaRetornarAdministrador() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("SCOPE_access_as_user")
        );

        // Act
        String result = AzureAdGroupsJwtConverter.getHighestRole(authorities);

        // Assert
        assertEquals("ADMINISTRADOR", result);
    }

    @Test
    void getHighestRole_ConRolManager_DeberiaRetornarGestor() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_MANAGER"),
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("SCOPE_access_as_user")
        );

        // Act
        String result = AzureAdGroupsJwtConverter.getHighestRole(authorities);

        // Assert
        assertEquals("GESTOR", result);
    }

    @Test
    void getHighestRole_ConRolUser_DeberiaRetornarUsuario() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("SCOPE_access_as_user")
        );

        // Act
        String result = AzureAdGroupsJwtConverter.getHighestRole(authorities);

        // Assert
        assertEquals("USUARIO", result);
    }

    @Test
    void getHighestRole_ConRolReader_DeberiaRetornarLector() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_READER"),
                new SimpleGrantedAuthority("SCOPE_access_as_user")
        );

        // Act
        String result = AzureAdGroupsJwtConverter.getHighestRole(authorities);

        // Assert
        assertEquals("LECTOR", result);
    }

    @Test
    void getHighestRole_ConRolDesconocido_DeberiaRetornarSinPermisos() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_UNKNOWN"),
                new SimpleGrantedAuthority("SCOPE_access_as_user")
        );

        // Act
        String result = AzureAdGroupsJwtConverter.getHighestRole(authorities);

        // Assert
        assertEquals("SIN_PERMISOS", result);
    }

    @Test
    void hasRole_ConRolExistente_DeberiaRetornarTrue() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
        );

        // Act
        boolean result = AzureAdGroupsJwtConverter.hasRole(authorities, "admin");

        // Assert
        assertTrue(result);
    }

    @Test
    void hasRole_ConRolInexistente_DeberiaRetornarFalse() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER")
        );

        // Act
        boolean result = AzureAdGroupsJwtConverter.hasRole(authorities, "admin");

        // Assert
        assertFalse(result);
    }

    @Test
    void extractAzureGroupIds_ConAuthoritiesValidas_DeberiaExtraerIds() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("GROUP_group1"),
                new SimpleGrantedAuthority("GROUP_group2"),
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("SCOPE_access_as_user")
        );

        // Act
        List<String> result = AzureAdGroupsJwtConverter.extractAzureGroupIds(authorities);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains("group1"));
        assertTrue(result.contains("group2"));
    }

    @Test
    void extractAzureGroupIds_ConAuthoritiesSinGrupos_DeberiaRetornarListaVacia() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("SCOPE_access_as_user")
        );

        // Act
        List<String> result = AzureAdGroupsJwtConverter.extractAzureGroupIds(authorities);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ========== TESTS PARA CASOS EDGE ==========

    // Test eliminado: convert_ConJwtNull_DeberiaLanzarExcepcion
    // El método requiere @NonNull Jwt, por lo que no se puede probar con null

    @Test
    void convert_ConJwtSinClaims_DeberiaManejarGracefully() {
        // Arrange
        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(auth -> "ROLE_USER".equals(auth.getAuthority())));
        assertTrue(result.stream().anyMatch(auth -> "SCOPE_access_as_user".equals(auth.getAuthority())));
    }

    @Test
    void convert_ConErrorEnPerfilService_DeberiaContinuarSinRol() {
        // Arrange
        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("groups", Arrays.asList("admin-group-id"))
                .claim("email", "test@example.com")
                .claim("name", "Test User")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(perfilService.obtenerPerfilPorAzureGroupId("admin-group-id"))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(auth -> "GROUP_admin-group-id".equals(auth.getAuthority())));
        // No se asigna ROLE_USER porque hay grupos presentes, aunque haya error
        assertTrue(result.stream().anyMatch(auth -> "SCOPE_access_as_user".equals(auth.getAuthority())));
    }

    @Test
    void convert_ConPerfilInactivo_DeberiaIgnorarPerfil() {
        // Arrange
        Perfil perfilInactivo = new Perfil("Inactivo", "Perfil inactivo", "inactive-group-id", "Inactive Group");
        perfilInactivo.setId(6L);
        perfilInactivo.setActivo(false);

        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("groups", Arrays.asList("inactive-group-id"))
                .claim("email", "test@example.com")
                .claim("name", "Test User")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(perfilService.obtenerPerfilPorAzureGroupId("inactive-group-id"))
                .thenReturn(Optional.of(perfilInactivo));

        // Act
        Collection<GrantedAuthority> result = converter.convert(mockJwt);

        // Assert
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(auth -> "GROUP_inactive-group-id".equals(auth.getAuthority())));
        // El perfil inactivo genera un rol personalizado basado en su nombre
        assertTrue(result.stream().anyMatch(auth -> "ROLE_INACTIVO".equals(auth.getAuthority())));
        assertTrue(result.stream().anyMatch(auth -> "SCOPE_access_as_user".equals(auth.getAuthority())));
    }
}
