package com.cluvy.auth.service;

import com.cluvy.auth.dto.AuthTokensResponse;
import com.cluvy.auth.dto.SocialLoginRequest;
import com.cluvy.auth.dto.SocialLoginResponse;

public interface AuthService {
    // 이메일/비밀번호 로그인
    AuthTokensResponse login(String email, String password);
    // 로그아웃
    void logout(String refreshToken);
    // Access Token 유효성 검증
    boolean validateToken(String accessToken);
    // 소셜 로그인
    SocialLoginResponse socialLogin(SocialLoginRequest request);
    // 카카오 API로부터 액세스 토큰 가져오기
    String getAccessTokenFromKakao(String code);
}
