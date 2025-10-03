package com.cluvy.auth.dto;

/*소셜 로그인 성공 시 클라이언트에게 Access Token, Refresh Token, 신규 가입 여부를 반환하는 응답 데이터*/

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SocialLoginResponse {
    private String accessToken;
    private String refreshToken;
    private boolean isNewUser;
}
