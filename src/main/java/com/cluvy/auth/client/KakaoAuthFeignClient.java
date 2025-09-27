package com.cluvy.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;

@FeignClient(name = "kakao-auth", url = "https://kauth.kakao.com")
public interface KakaoAuthFeignClient {

    /**
     * 카카오 OAuth 서버에 Access Token 발급을 요청
     *
     * @param grantType   OAuth2 인증 방식 (항상 authorization_code)
     * @param clientId    클라이언트 아이디
     * @param redirectUri 인가 코드 발급 시 사용한 redirect uri
     * @param code        인가 코드
     * @return Access Token, Refresh Token 등을 포함한 응답
     */
    @PostMapping(value = "/oauth/token", consumes = "application/x-www-form-urlencoded")
    Map<String, Object> getAccessToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("code") String code
    );
}
