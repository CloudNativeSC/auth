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
     * 카카오 소셜 로그인 콜백 엔드포인트
     *
     * 1. 카카오로부터 전달받은 인가 코드를 확인
     * 2. 인가 코드를 사용해 카카오 API에서 액세스 토큰 발급
     * 3. 액세스 토큰으로 소셜 로그인 처리 후 JWT 생성
     * 4. JWT를 쿠키에 담아 브라우저에 저장
     * 5. 브라우저를 프론트엔드 페이지로 리다이렉트
     *
     * 주의:
     * - 프론트엔드에서는 JSON body가 아니라 쿠키를 통해 로그인 상태를 확인
     * - 개발 환경에서는 Secure=false, 배포 시 HTTPS에서는 true로 설정 필요
     *
     * @param code 카카오에서 전달받은 인가 코드
     * @return 302 Redirect 응답 + JWT 쿠키
     */
    @GetMapping("/oauth")
    public ResponseEntity<?> kakaoCallback(@RequestParam String code) throws Exception {

        // 1. 인가 코드 확인
        log.info("인가 코드: {}", code);

        // 2. 카카오로 액세스 토큰 요청
        String accessToken = authService.getAccessTokenFromKakao(code);
        log.info("액세스 토큰: {}", accessToken);

        // 3. 카카오 로그인 처리
        SocialLoginResponse loginResponse = authService.socialLogin(
                new SocialLoginRequest("kakao", accessToken));
        log.info("로그인 응답: {}", loginResponse);

        // 4. 쿠키 생성 (HttpOnly, Secure, SameSite)
        // Access Token 쿠키
        ResponseCookie accessCookie = ResponseCookie.from("access_token", loginResponse.getAccessToken())
                .httpOnly(true)
                .secure(false) // HTTPS 환경에서는 true
                .path("/")
                .maxAge(60 * 60) // 1시간
                .sameSite("Lax")
                .build();

        // Refresh Token 쿠키
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", loginResponse.getRefreshToken())
                .httpOnly(true)
                .secure(false) // HTTPS 환경에서는 true
                .path("/")
                .maxAge(60 * 60 * 24 * 7)  // 7일
                .sameSite("Lax")
                .build();

        // 헤더에 두 쿠키 모두 추가
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        headers.add(HttpHeaders.LOCATION, "http://localhost:3000/"); // 프론트 주소

        // 6. 302 Redirect 응답 반환
        return ResponseEntity.status(HttpStatus.FOUND)
                .headers(headers)
                .build();
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
