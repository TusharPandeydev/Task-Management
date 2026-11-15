package com.campusconnect.campus.Dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendOtpDto {
    @NotBlank(message = "Email is required to resend OTP!")
    @Email(message = "Invalid email format")
    private String email;
}
