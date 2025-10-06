package com.example.apiprotegida.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para transferencia de datos de usuario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    
    private Long id;
    private String user;
    private String password;
    private List<String> permisos;
    
    // Campos adicionales para compatibilidad
    private String email;
    private String nombre;
    private String departamento;
    private String cargo;
    private Boolean activo;
}
