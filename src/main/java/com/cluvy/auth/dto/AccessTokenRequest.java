package com.cluvy.auth.dto;

/*클라이언트가 Access Token의 유효성 검증을 요청할 때 서버에 전달하는 데이터*/

import lombok.Getter;

@Getter
public class AccessTokenRequest {
    private String accessToken;
}
