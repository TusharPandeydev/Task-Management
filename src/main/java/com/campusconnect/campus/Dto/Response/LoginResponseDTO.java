package com.campusconnect.campus.Dto.Response;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String fullName;
    private String role;
    private String email;
    private String accessToken;
}
