package com.shopee.banking.bams.app.service.impl;

import com.shopee.banking.bams.app.service.dto.AuthTokens;
import com.shopee.banking.bams.common.exception.AuthException;
import com.shopee.banking.bams.common.exception.enums.AuthErrorCode;
import com.shopee.banking.bams.common.util.JwtUtils;
import com.shopee.banking.bams.domain.aggregateRoot.Admin;
import com.shopee.banking.bams.domain.aggregateRoot.JwtToken;
import com.shopee.banking.bams.domain.repository.IAdminRepository;
import com.shopee.banking.bams.domain.repository.IAuthTokenRepository;
import com.shopee.banking.bams.domain.valueObject.AdminId;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final Long ADMIN_ID = 1L;
    private static final String USERNAME = "test-admin";
    private static final String PASSWORD = "P@ssw0rd!";
    private static final String WRONG_PASSWORD = "wrong-password";
    private static final String JWT_SECRET = "test-jwt-secret";
    private static final long ACCESS_TOKEN_TTL_SECONDS = 600L;
    private static final long REFRESH_TOKEN_TTL_SECONDS = 3600L;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AuthServiceImpl authService;

    @Mock
    private IAdminRepository adminRepository;

    @Mock
    private IAuthTokenRepository authTokenRepository;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl();
        ReflectionTestUtils.setField(authService, "adminRepository", adminRepository);
        ReflectionTestUtils.setField(authService, "authTokenRepository", authTokenRepository);
        ReflectionTestUtils.setField(authService, "jwtSecret", JWT_SECRET);
        ReflectionTestUtils.setField(authService, "accessTokenTtlSeconds", ACCESS_TOKEN_TTL_SECONDS);
        ReflectionTestUtils.setField(authService, "refreshTokenTtlSeconds", REFRESH_TOKEN_TTL_SECONDS);
    }

    @Test
    @DisplayName("login succeeds with valid username and password")
    void login_validCredentials_returnsAccessAndRefreshTokens() {
        Admin admin = buildAdminWithPassword(passwordEncoder.encode(PASSWORD));
        when(adminRepository.queryByUsername(USERNAME)).thenReturn(admin);

        AuthTokens authTokens = authService.login(USERNAME, PASSWORD);

        assertNotNull(authTokens);
        assertTokenClaims(authTokens.getAccessToken().getToken(), String.valueOf(ADMIN_ID), USERNAME);
        assertTokenClaims(authTokens.getRefreshToken().getToken(), String.valueOf(ADMIN_ID), USERNAME);

        ArgumentCaptor<JwtToken> refreshTokenCaptor = ArgumentCaptor.forClass(JwtToken.class);
        verify(authTokenRepository).saveRefreshToken(refreshTokenCaptor.capture());
        JwtToken persistedRefreshToken = refreshTokenCaptor.getValue();
        assertEquals(ADMIN_ID, persistedRefreshToken.getAdminId().getId());
        assertEquals(authTokens.getRefreshToken().getToken(), persistedRefreshToken.getToken());
        assertTrue(persistedRefreshToken.getExpiresAt().isAfter(LocalDateTime.now()));
        assertTrue(!persistedRefreshToken.isRevoked());
    }

    @Test
    @DisplayName("login fails when password is incorrect")
    void login_invalidPassword_throwsInvalidCredentials() {
        when(adminRepository.queryByUsername(USERNAME))
                .thenReturn(buildAdminWithPassword(passwordEncoder.encode(PASSWORD)));

        AuthException exception = assertThrows(AuthException.class, () -> authService.login(USERNAME, WRONG_PASSWORD));

        assertEquals(AuthErrorCode.INVALID_CREDENTIALS, exception.getErrorType());
        verify(authTokenRepository, never()).saveRefreshToken(any(JwtToken.class));
    }

    @Test
    @DisplayName("login fails when username does not exist")
    void login_unknownUsername_throwsInvalidCredentials() {
        when(adminRepository.queryByUsername(USERNAME)).thenReturn(null);

        AuthException exception = assertThrows(AuthException.class, () -> authService.login(USERNAME, PASSWORD));

        assertEquals(AuthErrorCode.INVALID_CREDENTIALS, exception.getErrorType());
        verify(authTokenRepository, never()).saveRefreshToken(any(JwtToken.class));
    }

    @Test
    @DisplayName("refresh succeeds when token is valid and not revoked")
    void refresh_validToken_returnsNewAccessToken() {
        String refreshTokenValue = JwtUtils.createRefreshToken(ADMIN_ID, USERNAME, JWT_SECRET, REFRESH_TOKEN_TTL_SECONDS);
        JwtToken persistedRefreshToken = buildRefreshToken(refreshTokenValue, LocalDateTime.now().plusSeconds(REFRESH_TOKEN_TTL_SECONDS), false);
        Admin admin = buildAdminWithPassword(passwordEncoder.encode(PASSWORD));

        when(authTokenRepository.queryByRefreshToken(refreshTokenValue)).thenReturn(persistedRefreshToken);
        when(adminRepository.queryById(any(AdminId.class))).thenReturn(admin);

        JwtToken accessToken = authService.refresh(refreshTokenValue);

        assertNotNull(accessToken);
        assertTokenClaims(accessToken.getToken(), String.valueOf(ADMIN_ID), USERNAME);

        ArgumentCaptor<AdminId> adminIdCaptor = ArgumentCaptor.forClass(AdminId.class);
        verify(adminRepository).queryById(adminIdCaptor.capture());
        assertEquals(ADMIN_ID, adminIdCaptor.getValue().getId());
    }

    @Test
    @DisplayName("refresh fails when token is revoked")
    void refresh_revokedToken_throwsInvalidRefreshToken() {
        String refreshTokenValue = JwtUtils.createRefreshToken(ADMIN_ID, USERNAME, JWT_SECRET, REFRESH_TOKEN_TTL_SECONDS);
        when(authTokenRepository.queryByRefreshToken(refreshTokenValue))
                .thenReturn(buildRefreshToken(refreshTokenValue, LocalDateTime.now().plusSeconds(REFRESH_TOKEN_TTL_SECONDS), true));

        AuthException exception = assertThrows(AuthException.class, () -> authService.refresh(refreshTokenValue));

        assertEquals(AuthErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorType());
        verify(adminRepository, never()).queryById(any(AdminId.class));
    }

    @Test
    @DisplayName("refresh fails when token is expired and not revoked")
    void refresh_expiredToken_throwsInvalidRefreshToken() {
        String refreshTokenValue = JwtUtils.createRefreshToken(ADMIN_ID, USERNAME, JWT_SECRET, REFRESH_TOKEN_TTL_SECONDS);
        when(authTokenRepository.queryByRefreshToken(refreshTokenValue))
                .thenReturn(buildRefreshToken(refreshTokenValue, LocalDateTime.now().minusSeconds(1), false));

        AuthException exception = assertThrows(AuthException.class, () -> authService.refresh(refreshTokenValue));

        assertEquals(AuthErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorType());
        verify(adminRepository, never()).queryById(any(AdminId.class));
    }

    @Test
    @DisplayName("refresh fails when token is expired and revoked")
    void refresh_expiredAndRevokedToken_throwsInvalidRefreshToken() {
        String refreshTokenValue = JwtUtils.createRefreshToken(ADMIN_ID, USERNAME, JWT_SECRET, REFRESH_TOKEN_TTL_SECONDS);
        when(authTokenRepository.queryByRefreshToken(refreshTokenValue))
                .thenReturn(buildRefreshToken(refreshTokenValue, LocalDateTime.now().minusSeconds(1), true));

        AuthException exception = assertThrows(AuthException.class, () -> authService.refresh(refreshTokenValue));

        assertEquals(AuthErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorType());
        verify(adminRepository, never()).queryById(any(AdminId.class));
    }

    @Test
    @DisplayName("refresh fails when token subject does not match persisted admin id")
    void refresh_subjectMismatch_throwsInvalidRefreshToken() {
        String refreshTokenValue = JwtUtils.createRefreshToken(ADMIN_ID + 1, USERNAME, JWT_SECRET, REFRESH_TOKEN_TTL_SECONDS);
        when(authTokenRepository.queryByRefreshToken(refreshTokenValue))
                .thenReturn(buildRefreshToken(refreshTokenValue, LocalDateTime.now().plusSeconds(REFRESH_TOKEN_TTL_SECONDS), false));

        AuthException exception = assertThrows(AuthException.class, () -> authService.refresh(refreshTokenValue));

        assertEquals(AuthErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorType());
        verify(adminRepository, never()).queryById(any(AdminId.class));
    }

    @Test
    @DisplayName("refresh fails when token admin no longer exists")
    void refresh_adminNotFound_throwsInvalidRefreshToken() {
        String refreshTokenValue = JwtUtils.createRefreshToken(ADMIN_ID, USERNAME, JWT_SECRET, REFRESH_TOKEN_TTL_SECONDS);
        when(authTokenRepository.queryByRefreshToken(refreshTokenValue))
                .thenReturn(buildRefreshToken(refreshTokenValue, LocalDateTime.now().plusSeconds(REFRESH_TOKEN_TTL_SECONDS), false));
        when(adminRepository.queryById(any(AdminId.class))).thenReturn(null);

        AuthException exception = assertThrows(AuthException.class, () -> authService.refresh(refreshTokenValue));

        assertEquals(AuthErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorType());
    }

    @Test
    @DisplayName("refresh fails when token is not found in repository")
    void refresh_missingToken_throwsInvalidRefreshToken() {
        when(authTokenRepository.queryByRefreshToken(anyString())).thenReturn(null);

        AuthException exception = assertThrows(AuthException.class, () -> authService.refresh("missing-token"));

        assertEquals(AuthErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorType());
        verify(adminRepository, never()).queryById(any(AdminId.class));
    }

    private Admin buildAdminWithPassword(String hashedPassword) {
        return Admin.builder()
                .id(ADMIN_ID)
                .hashedPassword(hashedPassword)
                .username(USERNAME)
                .adminNickname("nickname")
                .adminProfilePictureUrl("https://example.com/avatar.png")
                .build();
    }

    private JwtToken buildRefreshToken(String token, LocalDateTime expiresAt, boolean revoked) {
        return new JwtToken(null, new AdminId(ADMIN_ID), token, null, expiresAt, revoked);
    }

    private void assertTokenClaims(String token, String expectedSubject, String expectedUsername) {
        Claims claims = JwtUtils.parseClaims(token, JWT_SECRET);
        assertEquals(expectedSubject, claims.getSubject());
        assertEquals(expectedUsername, claims.get(JwtUtils.USERNAME_CLAIM, String.class));
        assertEquals(JwtUtils.ROLE_ADMIN, claims.get(JwtUtils.ROLE_CLAIM, String.class));
    }
}
