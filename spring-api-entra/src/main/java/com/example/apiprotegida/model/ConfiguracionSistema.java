package com.example.apiprotegida.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad para almacenar configuraciones del sistema
 * Permite habilitar/deshabilitar features dinámicamente
 */
@Entity
@Table(name = "configuracion_sistema")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionSistema {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "clave", unique = true, nullable = false, length = 100)
    @NotBlank(message = "La clave es obligatoria")
    private String clave;
    
    @Column(name = "valor", nullable = false, length = 500)
    @NotBlank(message = "El valor es obligatorio")
    private String valor;
    
    @Column(name = "descripcion", length = 500)
    private String descripcion;
    
    @Column(name = "tipo", length = 50)
    private String tipo; // BOOLEAN, STRING, NUMBER, JSON
    
    @Column(name = "categoria", length = 50)
    private String categoria; // AUTENTICACION, SEGURIDAD, GENERAL
    
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
    
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
    
    /**
     * Obtiene el valor como booleano
     */
    public Boolean getValorBoolean() {
        return Boolean.parseBoolean(valor);
    }
    
    /**
     * Obtiene el valor como entero
     */
    public Integer getValorInteger() {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}


