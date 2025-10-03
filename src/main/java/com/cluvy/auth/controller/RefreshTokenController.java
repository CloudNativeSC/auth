package com.cluvy.auth.controller;

import com.cluvy.auth.dto.RefreshTokenRequest;
import com.cluvy.auth.dto.RefreshTokenResponse;
import com.cluvy.auth.entity.RefreshToken;
import com.cluvy.auth.response.ApiResponse;
import com.cluvy.auth.service.RefreshTokenService;
import com.cluvy.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;

    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = refreshTokenService.refresh(request);
        return ApiResponse.onSuccess(response);
    }
}
