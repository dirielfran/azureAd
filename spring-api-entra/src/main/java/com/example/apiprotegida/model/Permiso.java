package com.example.apiprotegida.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Permiso que representa los permisos del sistema
 * Cada permiso puede estar asociado a múltiples perfiles
 */
@Entity
@Table(name = "permisos")
public class Permiso {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El código del permiso es obligatorio")
    @Size(min = 2, max = 50, message = "El código debe tener entre 2 y 50 caracteres")
    @Column(nullable = false, unique = true)
    private String codigo; // Ej: "USUARIOS_LEER", "REPORTES_CREAR", etc.
    
    @NotBlank(message = "El nombre del permiso es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false)
    private String nombre;
    
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    @Column
    private String descripcion;
    
    @NotBlank(message = "El módulo es obligatorio")
    @Size(max = 50, message = "El módulo no puede exceder 50 caracteres")
    @Column(nullable = false)
    private String modulo; // Ej: "USUARIOS", "REPORTES", "CONFIGURACION"
    
    @NotBlank(message = "La acción es obligatoria")
    @Size(max = 20, message = "La acción no puede exceder 20 caracteres")
    @Column(nullable = false)
    private String accion; // Ej: "LEER", "CREAR", "EDITAR", "ELIMINAR"
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @Column
    private Boolean activo = true;
    
    // Relación Many-to-Many con Perfil (lado inverso)
    @ManyToMany(mappedBy = "permisos", fetch = FetchType.LAZY)
    @JsonIgnore // Evitar referencias circulares en JSON
    private Set<Perfil> perfiles = new HashSet<>();

    // Constructores
    public Permiso() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    public Permiso(String codigo, String nombre, String descripcion, String modulo, String accion) {
        this();
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.modulo = modulo;
        this.accion = accion;
    }

    // Métodos de ciclo de vida
    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Set<Perfil> getPerfiles() {
        return perfiles;
    }

    public void setPerfiles(Set<Perfil> perfiles) {
        this.perfiles = perfiles;
    }

    // toString
    @Override
    public String toString() {
        return "Permiso{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", modulo='" + modulo + '\'' +
                ", accion='" + accion + '\'' +
                ", activo=" + activo +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permiso)) return false;
        Permiso permiso = (Permiso) o;
        return codigo.equals(permiso.codigo);
    }

    @Override
    public int hashCode() {
        return codigo.hashCode();
    }
}
