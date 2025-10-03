package com.cluvy.auth.controller;

import com.cluvy.auth.dto.*;
import com.cluvy.auth.response.ApiResponse;
import com.cluvy.auth.service.AuthService;
import com.cluvy.auth.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

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
     * 카카오 소셜 로그인 엔드포인트
     *
     * 1. 요청 바디에서 카카오 인가 코드를 확인
     * 2. 인가 코드를 사용해 카카오 API로부터 액세스 토큰 발급
     * 3. 발급받은 액세스 토큰으로 소셜 로그인 처리 후 JWT 생성
     * 4. access token은 응답 body에, refresh token은 HttpOnly 쿠키에 저장
     * 5. 브라우저에서 쿠키를 통해 로그인 상태 유지
     *
     * 주의:
     * - 프론트엔드는 응답 body 대신 쿠키(refresh_token)로 로그인 상태를 확인
     * - 개발 환경에서는 secure=false, 배포 환경(HTTPS)에서는 secure=true로 설정 필요
     *
     * @param request 카카오 인가 코드가 포함된 SocialLoginRequest
     * @return JWT 포함 body와 refresh token 쿠키를 담은 ResponseEntity
     */
    @PostMapping("/login/kakao")
    public ResponseEntity<ApiResponse<SocialLoginResponse>> socialLogin(@RequestBody SocialLoginRequest request) {
        String code = request.getAccessToken(); // 액세스 토큰이라 작성했지만 사실 이건 인가 코드
        log.info("인가 코드: {}", code);

        String accessToken = authService.getAccessTokenFromKakao(code);
        log.info("액세스 토큰: {}", accessToken);

        SocialLoginResponse response = authService.socialLogin(new SocialLoginRequest("kakao", accessToken));
        log.info("login res: {}", response);

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", response.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 60 * 24 * 7)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.onSuccess(response));
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
