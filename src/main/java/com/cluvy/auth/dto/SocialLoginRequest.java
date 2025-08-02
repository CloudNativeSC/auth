package com.cluvy.auth.dto;

/*소셜 로그인 시 소셜 플랫폼 토큰과 provider 정보를 서버에 전달하는 요청 데이터*/

import lombok.Getter;

@Getter
public class SocialLoginRequest {
    private String provider;  // kakao, google
    private String accessToken; // 소셜 플랫폼에서 받은 토큰
}
