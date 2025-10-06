package com.example.apiprotegida.shared.dto.responses;

import com.example.apiprotegida.shared.dto.UserDTO;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO de respuesta para información de usuario
 */
@Data
public class ResponseUserDTO {
    
    private UserDTO user;
    private boolean error = false;
    private Map<String, String> errorMessages = new HashMap<>();
    
    public void addErrorMessage(String key, String message) {
        this.errorMessages.put(key, message);
        this.error = true;
    }
}
