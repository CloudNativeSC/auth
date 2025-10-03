package com.cluvy.auth.dto;

/*소셜 로그인 시 소셜 플랫폼에서 받아온 사용자 정보를 담는 데이터*/

import lombok.Data;

@Data
public class SocialUserInfo {
    private String id;                // 카카오 회원번호 (String 또는 Long)
    private String email;             // 이메일 (동의 시)
    private String nickname;          // 닉네임 (동의 시)
    private String profileImageUrl;   // 프로필 이미지 (동의 시)
    private String provider;          // "kakao"
}
