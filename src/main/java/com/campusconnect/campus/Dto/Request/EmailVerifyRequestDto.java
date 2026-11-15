package com.campusconnect.campus.Dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;



@Data
public class EmailVerifyRequestDto {
    @NotBlank(message = "email should be there")
    @Email(message = "invalid email format")
    private String email;

    @NotBlank(message ="Please enter the OTP!")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be exactly 6 digits")
    // very important thingh is learn today pattern is applied only in strings
    private String otp;
}
