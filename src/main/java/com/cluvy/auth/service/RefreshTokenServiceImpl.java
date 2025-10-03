package com.cluvy.auth.service;

import com.cluvy.auth.dto.RefreshTokenRequest;
import com.cluvy.auth.dto.RefreshTokenResponse;
import com.cluvy.auth.entity.RefreshToken;
import com.cluvy.auth.repository.RefreshTokenRepository;
import com.cluvy.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void saveRefreshToken(Long userId, String tokenHash, String deviceInfo, String ipAddress, LocalDateTime expiresAt) {
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .expiresAt(expiresAt)
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return refreshTokenRepository.findByTokenHash(tokenHash);
    }

    @Override
    public void revokeToken(String tokenHash) {
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    @Override
    public void deleteByTokenHash(String tokenHash) {
        refreshTokenRepository.deleteByTokenHash(tokenHash);
    }

    @Override
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        String tokenHash = jwtUtil.hashToken(refreshToken);

        RefreshToken tokenEntity = refreshTokenRepository.findByTokenHash(tokenHash)
                .filter(rt -> !rt.isRevoked() && rt.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new RuntimeException("유효하지 않은 Refresh Token"));

        Long userId = tokenEntity.getUserId();
        String newAccessToken = jwtUtil.generateAccessToken(userId.toString());
        String newRefreshToken = jwtUtil.generateRefreshToken(userId.toString());
        String newTokenHash = jwtUtil.hashToken(newRefreshToken);

        // 기존 토큰 폐기
        tokenEntity.setRevoked(true);
        refreshTokenRepository.save(tokenEntity);

        // 새 토큰 저장
        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                Instant.now().plusMillis(jwtUtil.getRefreshExpiration()),
                ZoneId.systemDefault()
        );

        RefreshToken newToken = RefreshToken.builder()
                .userId(userId)
                .tokenHash(newTokenHash)
                .deviceInfo(request.getDeviceInfo())
                .ipAddress(request.getIpAddress())
                .expiresAt(expiresAt)
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(newToken);

        return new RefreshTokenResponse(newAccessToken, newRefreshToken);
    }
}