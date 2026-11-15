package com.campusconnect.campus.Dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @NotBlank(message="Email is required for LOGIN!")
    @Email(message = "email format is incorrect")
    private String email;
    @NotBlank(message="Password is required for LOGIN")
    private String password;
}
