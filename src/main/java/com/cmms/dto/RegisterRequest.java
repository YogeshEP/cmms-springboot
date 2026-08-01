package com.cmms.dto;

import com.cmms.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @Email(message = "Valid email is required")
    private String email;

    // Optional - defaults to TECHNICIAN if not supplied
    private Role role;
}
