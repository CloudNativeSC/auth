package com.cluvy.auth.service;

import com.cluvy.auth.client.KakaoFeignClient;
import com.cluvy.auth.client.UserFeignClient;
import com.cluvy.auth.dto.*;
import com.cluvy.auth.exception.GeneralException;
import com.cluvy.auth.response.status.ErrorStatus;
import com.cluvy.auth.util.JwtUtil;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserFeignClient userFeignClient;
    private final KakaoAuthService kakaoAuthService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final RefreshTokenService refreshTokenService;

    /**
     * 이메일/비밀번호 기반 로그인 처리
     */
    @Override
    public AuthTokensResponse login(String email, String password) {
        UserVerifyRequest request = new UserVerifyRequest();
        request.setEmail(email);
        request.setAuthType("email");
        UserVerifyResponse user = userFeignClient.verifyUser(request);

        if (user == null) {
            throw new GeneralException(ErrorStatus.USER_NOT_FOUND);
        }
        if (!user.isEmailVerified()) {
            throw new GeneralException(ErrorStatus.EMAIL_NOT_VERIFIED);
        }
        /*if (!"active".equalsIgnoreCase(user.getStatus())) {
            throw new GeneralException(ErrorStatus.USER_INACTIVE);
        }*/
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new GeneralException(ErrorStatus.INVALID_PASSWORD);
        }

        if (!"active".equalsIgnoreCase(user.getStatus())) {
            userFeignClient.reactivateUser(user.getId());
            user.setStatus("active");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId().toString());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId().toString());

        String tokenHash = jwtUtil.hashToken(refreshToken);
        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                Instant.now().plusMillis(jwtUtil.getRefreshExpiration()),
                ZoneId.systemDefault()
        );
        refreshTokenService.saveRefreshToken(
                user.getId(),
                tokenHash,
                null,
                null,
                expiresAt
        );

        userFeignClient.updateLastLoginAt("Bearer " + accessToken);

        return new AuthTokensResponse(accessToken, refreshToken);

    }

    /**
     * 로그아웃 처리 (Refresh Token 폐기 등)
     */
    @Override
    public void logout(String refreshToken) {
        // refreshToken 폐기(블랙리스트 처리 등) 로직은 별도 서비스에서 처리
    }

    /**
     * Access Token의 유효성 검증
     */
    @Override
    public boolean validateToken(String accessToken) {
        return jwtUtil.validateToken(accessToken);
    }

    /**
     * 소셜 로그인 처리
     */
    @Override
    public SocialLoginResponse socialLogin(SocialLoginRequest request) {
        SocialUserInfo userInfo = kakaoAuthService.getKakaoUserInfo(request.getAccessToken());
        boolean isNewUser = userFeignClient.registerOrFindSocialUser(userInfo);
        String accessToken = jwtUtil.generateAccessToken(userInfo.getId());
        String refreshToken = jwtUtil.generateRefreshToken(userInfo.getId());

        userFeignClient.updateLastLoginAt("Bearer " + accessToken);

        return new SocialLoginResponse(accessToken, refreshToken, isNewUser);
    }

}
