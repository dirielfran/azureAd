package com.example.apiprotegida.shared.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para tokens JWT
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseJWTDTO {
    
    private String token;
    private String type = "Bearer";
    private String message = "Autenticación exitosa";
    
    public ResponseJWTDTO(String token) {
        this.token = token;
    }
}
