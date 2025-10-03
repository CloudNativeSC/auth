package com.cluvy.auth.service;

import com.cluvy.auth.client.UserFeignClient;
import com.cluvy.auth.dto.*;
import com.cluvy.auth.exception.GeneralException;
import com.cluvy.auth.response.status.ErrorStatus;
import com.cluvy.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
     * 카카오 로그인 처리
     *
     * 1. 액세스 토큰을 받아서 카카오 API로 사용자 정보 요청
     * 2. user 서비스로 사용자 정보를 보내 사용자 조회 또는 등록
     * 3. 사용자 아이디로 토큰 생성
     * 4. refresh token을 DB에 저장
     * 5. 토큰과 신규 가입 유저인지 판단하는 데이터 반환
     *
     * @param request 카카오 API로부터 받은 인가 코드를 포함한 SocialLoginRequest
     * @return 토큰과 신규 유저 확인 데이터를 포함한 SocialLoginResponse
     */
    @Override
    public SocialLoginResponse socialLogin(SocialLoginRequest request) {
        SocialUserInfo userInfo = kakaoAuthService.getKakaoUserInfo(request.getAccessToken());
        boolean isNewUser = userFeignClient.registerOrFindSocialUser(userInfo);

        String accessToken = jwtUtil.generateAccessToken(userInfo.getId());
        String refreshToken = jwtUtil.generateRefreshToken(userInfo.getId());
        String tokenHash = jwtUtil.hashToken(refreshToken);
        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                Instant.now().plusMillis(jwtUtil.getRefreshExpiration()),
                ZoneId.systemDefault()
        );

        refreshTokenService.saveRefreshToken(
                Long.valueOf(userInfo.getId()),
                tokenHash,
                null,
                null,
                expiresAt
        );
        userFeignClient.updateLastLoginAt("Bearer " + accessToken);

        return new SocialLoginResponse(accessToken, refreshToken, isNewUser);
    }

    /**
     * 카카오 API로부터 액세스 토큰 가져오기
     * 
     * @param code 인가 코드
     * @return 카카오 API로부터 받은 액세스 토큰
     */
    @Override
    public String getAccessTokenFromKakao(String code) {
        return kakaoAuthService.getKakaoAccessToken(code);
    }

}
