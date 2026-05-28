package com.shopee.banking.bams.app.service.impl;

import com.shopee.banking.bams.app.service.IAuthService;
import com.shopee.banking.bams.app.service.dto.AuthTokens;
import com.shopee.banking.bams.app.utils.PasswordUtils;
import com.shopee.banking.bams.common.enums.AuthErrorCode;
import com.shopee.banking.bams.common.factory.ExceptionFactory;
import com.shopee.banking.bams.common.util.Asserter;
import com.shopee.banking.bams.app.utils.JwtUtils;
import com.shopee.banking.bams.domain.JwtToken;
import com.shopee.banking.bams.domain.aggregateRoot.Admin;
import com.shopee.banking.bams.domain.repository.IAdminRepository;
import com.shopee.banking.bams.domain.repository.IAuthTokenRepository;
import com.shopee.banking.bams.domain.valueObject.AdminId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
public class AuthServiceImpl implements IAuthService {

    @Autowired
    private IAdminRepository adminRepository;

    @Autowired
    private IAuthTokenRepository authTokenRepository;

    @Value("${bams.auth.jwt.secret}")
    private String jwtSecret;

    @Value("${bams.auth.jwt.access-token-ttl-seconds}")
    private long accessTokenTtlSeconds;

    @Value("${bams.auth.jwt.refresh-token-ttl-seconds}")
    private long refreshTokenTtlSeconds;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public AuthTokens login(String username, String password) {
        Admin admin = adminRepository.queryByUsername(username);
        Asserter.assertTrue(
                admin != null && PasswordUtils.passwordMatches(password, admin.getHashedPassword()),
                AuthErrorCode.INVALID_CREDENTIALS
        );

        JwtToken accessToken = buildAccessToken(admin);
        JwtToken refreshToken = buildRefreshToken(admin);
        authTokenRepository.saveRefreshToken(refreshToken);

        AuthTokens authTokens = new AuthTokens();
        authTokens.setAccessToken(accessToken);
        authTokens.setRefreshToken(refreshToken);
        return authTokens;
    }

    @Override
    public JwtToken refresh(String refreshToken) {
        JwtToken persistedToken = authTokenRepository.queryByRefreshToken(refreshToken);
        Asserter.assertTrue(
                persistedToken != null && persistedToken.isValid(),
                AuthErrorCode.INVALID_REFRESH_TOKEN
        );

        Claims claims = parseRefreshToken(refreshToken);
        String adminId = String.valueOf(persistedToken.getAdminId().getId());
        Asserter.assertTrue(
                adminId.equals(claims.getSubject()),
                AuthErrorCode.INVALID_REFRESH_TOKEN
        );

        Admin admin = adminRepository.queryById(persistedToken.getAdminId());
        Asserter.assertTrue(admin != null, AuthErrorCode.INVALID_REFRESH_TOKEN);
        return buildAccessToken(admin);
    }

    @Override
    public AdminId verifyAccessToken(String accessToken) {
        try {
            Claims claims = JwtUtils.parseClaims(accessToken, jwtSecret);
            Asserter.assertTrue(
                    Objects.equals(JwtUtils.ROLE_ADMIN, claims.get(JwtUtils.ROLE_CLAIM, String.class)),
                    AuthErrorCode.INVALID_ACCESS_TOKEN
            );
            return new AdminId(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            ExceptionFactory.throwException(AuthErrorCode.INVALID_ACCESS_TOKEN);
            return null;
        }
    }

    private JwtToken buildAccessToken(Admin admin) {
        String token = JwtUtils.createAccessToken(admin.getId(), admin.getUsername(), jwtSecret, accessTokenTtlSeconds);
        return new JwtToken(token);
    }

    private JwtToken buildRefreshToken(Admin admin) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenTtlSeconds);
        String token = JwtUtils.createRefreshToken(admin.getId(), admin.getUsername(), jwtSecret, refreshTokenTtlSeconds);
        return new JwtToken(null, new AdminId(admin.getId()), token, null, expiresAt, false);
    }

    private Claims parseRefreshToken(String refreshToken) {
        try {
            return JwtUtils.parseClaims(refreshToken, jwtSecret);
        } catch (JwtException | IllegalArgumentException e) {
            ExceptionFactory.throwException(AuthErrorCode.INVALID_REFRESH_TOKEN);
            return null;
        }
    }

}
