package com.campusconnect.campus.Dto.Response;

import lombok.Data;

@Data
public class ValidateRefreshTokenDto {
    String accessToken;
    String refreshToken;
}
