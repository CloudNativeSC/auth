package com.cluvy.auth.service;

import com.cluvy.auth.client.KakaoFeignClient;
import com.cluvy.auth.dto.SocialUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final KakaoFeignClient kakaoFeignClient;

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
}