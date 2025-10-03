package com.cluvy.auth.service;

import com.cluvy.auth.client.KakaoAuthFeignClient;
import com.cluvy.auth.client.KakaoFeignClient;
import com.cluvy.auth.dto.SocialUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final KakaoFeignClient kakaoFeignClient;
    private final KakaoAuthFeignClient kakaoAuthFeignClient;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    public SocialUserInfo getKakaoUserInfo(String accessToken) {
        Map<String, Object> body = kakaoFeignClient.getUserInfo("Bearer " + accessToken);
        Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
        Map<String, Object> profile = kakaoAccount != null && kakaoAccount.containsKey("profile")
                ? (Map<String, Object>) kakaoAccount.get("profile")
                : null;

        SocialUserInfo userInfo = new SocialUserInfo();
        userInfo.setId(String.valueOf(body.get("id")));
        userInfo.setEmail(kakaoAccount != null ? (String) kakaoAccount.get("email") : null);
        userInfo.setNickname(profile != null ? (String) profile.get("nickname") : null);
        userInfo.setProfileImageUrl(profile != null ? (String) profile.get("profile_image_url") : null);
        userInfo.setProvider("kakao");
        return userInfo;
    }

    /**
     * 카카오 OAuth 서버에 인가 코드를 전달하여 액세스 토큰을 발급받음
     *
     * @param code 카카오 인증 과정에서 발급받은 인가 코드
     * @return 카카오 액세스 토큰
     */
    public String getKakaoAccessToken(String code) {
        Map<String, Object> response = kakaoAuthFeignClient.getAccessToken(
                "authorization_code",
                clientId,
                redirectUri,
                code
        );

        return (String) response.get("access_token");
    }
}