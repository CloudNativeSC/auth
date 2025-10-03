package com.cluvy.auth.controller;

import com.cluvy.auth.dto.RefreshTokenRequest;
import com.cluvy.auth.dto.RefreshTokenResponse;
import com.cluvy.auth.response.ApiResponse;
import com.cluvy.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;

    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refresh(@CookieValue(name = "refresh_token") String refreshToken) {
        RefreshTokenResponse response = refreshTokenService.refresh(new RefreshTokenRequest(refreshToken, null, null));
        return ApiResponse.onSuccess(response);
    }
}
