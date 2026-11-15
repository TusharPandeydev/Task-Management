package com.campusconnect.campus.Dto.Response;

import lombok.Data;

@Data
public class RegisterResponseDTO {
    private String fullName;
    private String phoneNumber;
    private String email;
    private String message;
}
