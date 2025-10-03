package com.cluvy.auth.service;

import com.cluvy.auth.dto.RefreshTokenRequest;
import com.cluvy.auth.dto.RefreshTokenResponse;
import com.cluvy.auth.entity.RefreshToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenService {
    void saveRefreshToken(Long userId, String tokenHash, String deviceInfo, String ipAddress, LocalDateTime expiresAt);
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    void revokeToken(String tokenHash);
    void deleteByTokenHash(String tokenHash);
    RefreshTokenResponse refresh(RefreshTokenRequest request);
}
