package com.campusconnect.campus.Dto.Response;

import lombok.Data;

@Data
public class AuthTokenDto {
    private String accessToken;
    private String refreshToken;
    private String userName;
    private String emailID;
    private String userRole;
}
