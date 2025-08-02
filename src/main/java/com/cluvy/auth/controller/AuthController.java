package com.cluvy.auth.controller;

import com.cluvy.auth.dto.*;
import com.cluvy.auth.response.ApiResponse;
import com.cluvy.auth.service.AuthService;
import com.cluvy.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    /**
     * 일반 로그인
     */
    @PostMapping("/login")
    public ApiResponse<AuthTokensResponse> login(@RequestBody LoginRequest request) {
        AuthTokensResponse tokens = authService.login(request.getEmail(), request.getPassword());
        return ApiResponse.onSuccess(tokens);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request) {
        authService.logout(request.getRefreshToken());
        return ApiResponse.onSuccess(null);
    }

    /**
     * Access Token 유효성 검증
     */
    @PostMapping("/validate")
    public ApiResponse<ValidateResponse> validate(@RequestBody AccessTokenRequest request) {
        boolean valid = authService.validateToken(request.getAccessToken());
        Long userId = null;
        if (valid) {
            userId = jwtUtil.getUserIdFromToken(request.getAccessToken());
        }
        return ApiResponse.onSuccess(new ValidateResponse(valid, userId));
    }

    /**
     * 소셜 로그인
     */
    @PostMapping("/login/kakao")
    public ApiResponse<SocialLoginResponse> socialLogin(@RequestBody SocialLoginRequest request) {
        SocialLoginResponse response = authService.socialLogin(request);
        return ApiResponse.onSuccess(response);
    }

    /**
     * 카카오 인가 코드 콜백 (카카오 로그인 테스트용)
     */
    @GetMapping("/oauth")
    public String kakaoCallback(@RequestParam String code) {
        // 실제 서비스에서는 code로 카카오 토큰 발급 요청 등 추가 처리 필요
        return "인가 코드: " + code;
    }

    /**
     * JWT에서 userId 추출
     */
    @GetMapping("/me")
    public ResponseEntity<Long> getUserIdFromToken(@RequestHeader("Authorization") String authorizationHeader) {
        // "Bearer ..."에서 토큰만 추출
        String token = authorizationHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        return ResponseEntity.ok(userId);
    }

    // 카카오 소셜 회원용
    @GetMapping("/me/kakao")
    public String getSocialIdFromToken(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        return jwtUtil.getSocialIdFromToken(token);
    }

}
