package com.example.apiprotegida.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotaciones personalizadas para simplificar la autorización por roles
 */
public class RoleAnnotations {

    /**
     * Permite acceso solo a usuarios con rol ADMIN
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasRole('ADMIN')")
    public @interface AdminOnly {
    }

    /**
     * Permite acceso a usuarios con rol ADMIN o MANAGER
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public @interface AdminOrManager {
    }

    /**
     * Permite acceso a usuarios con rol ADMIN, MANAGER o USER
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public @interface AdminManagerOrUser {
    }

    /**
     * Permite acceso a cualquier usuario autenticado con roles válidos
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER', 'READER')")
    public @interface AnyValidRole {
    }

    /**
     * Permite acceso solo a usuarios con scope access_as_user
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAuthority('SCOPE_access_as_user')")
    public @interface ValidScope {
    }

    /**
     * Combinación de scope válido y cualquier rol válido
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAuthority('SCOPE_access_as_user') and hasAnyRole('ADMIN', 'MANAGER', 'USER', 'READER')")
    public @interface ValidScopeAndRole {
    }
}
