package com.cluvy.auth.client;

import com.cluvy.auth.dto.SocialUserInfo;
import com.cluvy.auth.dto.UserVerifyRequest;
import com.cluvy.auth.dto.UserVerifyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user", url = "http://localhost:8081") // 임시 포트
public interface UserFeignClient {
    /**
     * 사용자 정보 검증/조회 (로그인, 인증 등)
     */
    @PostMapping("/api/users/verify")
    UserVerifyResponse verifyUser(@RequestBody UserVerifyRequest request);

    /**
     * 소셜 로그인 시 소셜 계정 등록 또는 기존 회원 조회
     */
    @PostMapping("/api/users/social")
    boolean registerOrFindSocialUser(@RequestBody SocialUserInfo userInfo);

    @PutMapping("/api/users/last-login")
    void updateLastLoginAt(@RequestHeader("Authorization") String authorizationHeader);

    @PutMapping("/api/users/{id}/reactivate")
    void reactivateUser(@PathVariable("id") Long userId);

}
