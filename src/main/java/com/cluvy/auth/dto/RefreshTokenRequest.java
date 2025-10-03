package com.cluvy.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshTokenRequest {
    @NotBlank
    private String refreshToken;
    private String deviceInfo;
    private String ipAddress;
}
