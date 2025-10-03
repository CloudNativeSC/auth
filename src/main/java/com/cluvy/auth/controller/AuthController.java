package com.cluvy.auth.controller;

import com.cluvy.auth.dto.*;
import com.cluvy.auth.response.ApiResponse;
import com.cluvy.auth.service.AuthService;
import com.cluvy.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
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
     * 소셜 로그인 (현재는 사용하지 않지만 남겨둠)
     */
    @PostMapping("/login/kakao")
    public ApiResponse<SocialLoginResponse> socialLogin(@RequestBody SocialLoginRequest request) {
        SocialLoginResponse response = authService.socialLogin(request);
        return ApiResponse.onSuccess(response);
    }

    /**
     * 카카오 인가 코드를 받고, 카카오 API를 호출해 엑세스 토큰을 받은 후 소셜 로그인
     *
     * @param code 인가 코드
     * @return jwt와 refresh token을 포함한 SocialLoginResponse
     */
    @GetMapping("/oauth")
    public ApiResponse<SocialLoginResponse> kakaoCallback(@RequestParam String code) throws Exception {
        // kauth로 액세스 토큰 요청
        log.info("인가 코드: {}", code);
        String accessToken = authService.getAccessTokenFromKakao(code);

        // kapi로 카카오 로그인
        log.info("액세스 토큰: " + accessToken);
        SocialLoginResponse loginResponse = authService.socialLogin(new SocialLoginRequest("kakao", accessToken));

        // jwt 포함 응답
        log.info("로그인 응답: {}", loginResponse);
        return ApiResponse.onSuccess(loginResponse);
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
