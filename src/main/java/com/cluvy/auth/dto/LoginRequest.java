package com.cluvy.auth.dto;

/*사용자가 로그인할 때 Auth 서비스에 보내는 요청 데이터*/

import lombok.Getter;

@Getter
public class LoginRequest {
    private String email; // 이메일 로그인 시
    private String password;
    private String authType; // email, kakao, google
    private String socialId; // 소셜 로그인 시
}
