package com.cluvy.auth.dto;

/*로그인/소셜 로그인/토큰 재발급 성공 시 클라이언트에게 Access Token, Refresh Token을 반환하는 응답 데이터*/

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokensResponse {
    private String accessToken;
    private String refreshToken;
}
