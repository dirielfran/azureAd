package com.example.apiprotegida.repository;

import com.example.apiprotegida.model.PasswordResetToken;
import com.example.apiprotegida.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repositorio para tokens de recuperación de contraseña
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Busca un token por su valor
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Busca tokens válidos (no usados y no expirados) para un usuario
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.usuario = :usuario AND t.usado = false AND t.fechaExpiracion > :ahora")
    Optional<PasswordResetToken> findTokenValidoPorUsuario(@Param("usuario") Usuario usuario, @Param("ahora") LocalDateTime ahora);

    /**
     * Marca todos los tokens de un usuario como usados
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.usado = true WHERE t.usuario = :usuario AND t.usado = false")
    void invalidarTokensDelUsuario(@Param("usuario") Usuario usuario);

    /**
     * Elimina tokens expirados (limpieza periódica)
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.fechaExpiracion < :ahora")
    void eliminarTokensExpirados(@Param("ahora") LocalDateTime ahora);

    /**
     * Cuenta tokens válidos para un usuario (para rate limiting)
     */
    @Query("SELECT COUNT(t) FROM PasswordResetToken t WHERE t.usuario = :usuario AND t.usado = false AND t.fechaCreacion > :desde")
    long contarTokensRecientes(@Param("usuario") Usuario usuario, @Param("desde") LocalDateTime desde);
}
