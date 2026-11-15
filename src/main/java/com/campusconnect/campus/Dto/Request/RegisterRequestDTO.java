package com.campusconnect.campus.Dto.Request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "Name is required!")
    private String fullName;

    @NotBlank(message = "Please enter Email ID!")
    @Email(message = "Invalid Email Format!")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian phone number. Must be 10 digits starting with 6-9.")
    // for Indian pattern numbers only
    private String phoneNumber;

    @NotBlank(message = "Password is mandatory!")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;


    // student field

}
