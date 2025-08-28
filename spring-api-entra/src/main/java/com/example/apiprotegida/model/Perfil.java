package com.example.apiprotegida.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Perfil que representa los perfiles/roles del sistema
 * Cada perfil tiene múltiples permisos asociados
 */
@Entity
@Table(name = "perfiles")
public class Perfil {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El nombre del perfil es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Column(nullable = false, unique = true)
    private String nombre;
    
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    @Column
    private String descripcion;
    
    @Column(name = "azure_group_id")
    private String azureGroupId; // ID del grupo de Azure AD
    
    @Column(name = "azure_group_name")
    private String azureGroupName; // Nombre del grupo de Azure AD
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @Column
    private Boolean activo = true;
    
    // Relación Many-to-Many con Permiso
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "perfil_permisos",
        joinColumns = @JoinColumn(name = "perfil_id"),
        inverseJoinColumns = @JoinColumn(name = "permiso_id")
    )
    private Set<Permiso> permisos = new HashSet<>();

    // Constructores
    public Perfil() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    public Perfil(String nombre, String descripcion) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Perfil(String nombre, String descripcion, String azureGroupId, String azureGroupName) {
        this(nombre, descripcion);
        this.azureGroupId = azureGroupId;
        this.azureGroupName = azureGroupName;
    }

    // Métodos de ciclo de vida
    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    // Métodos de utilidad
    public void addPermiso(Permiso permiso) {
        this.permisos.add(permiso);
        permiso.getPerfiles().add(this);
    }

    public void removePermiso(Permiso permiso) {
        this.permisos.remove(permiso);
        permiso.getPerfiles().remove(this);
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getAzureGroupId() {
        return azureGroupId;
    }

    public void setAzureGroupId(String azureGroupId) {
        this.azureGroupId = azureGroupId;
    }

    public String getAzureGroupName() {
        return azureGroupName;
    }

    public void setAzureGroupName(String azureGroupName) {
        this.azureGroupName = azureGroupName;
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

    public Set<Permiso> getPermisos() {
        return permisos;
    }

    public void setPermisos(Set<Permiso> permisos) {
        this.permisos = permisos;
    }

    // toString
    @Override
    public String toString() {
        return "Perfil{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", azureGroupId='" + azureGroupId + '\'' +
                ", azureGroupName='" + azureGroupName + '\'' +
                ", activo=" + activo +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}
