package com.cluvy.auth.dto;

/*Auth 서비스가 User 서비스에 사용자 정보 검증을 요청할 때 사용하는 데이터*/

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserVerifyRequest {
    private String email;
    private String username;
    private String authType;
    private String socialId;
}
