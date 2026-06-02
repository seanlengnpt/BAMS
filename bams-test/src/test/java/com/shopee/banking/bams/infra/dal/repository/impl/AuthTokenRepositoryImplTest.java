package com.shopee.banking.bams.infra.dal.repository.impl;

import com.shopee.banking.bams.common.exception.DependencyException;
import com.shopee.banking.bams.common.exception.ParamException;
import com.shopee.banking.bams.common.exception.enums.DependencyErrorCode;
import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;
import com.shopee.banking.bams.domain.aggregateRoot.JwtToken;
import com.shopee.banking.bams.domain.valueObject.AdminId;
import com.shopee.banking.bams.infra.dal.converter.AdminTokenDataConverter;
import com.shopee.banking.bams.infra.dal.dataObject.AdminTokenDO;
import com.shopee.banking.bams.infra.dal.mapper.AdminTokenMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthTokenRepositoryImplTest {

    private static final Long TOKEN_ID = 10L;
    private static final Long ADMIN_ID = 1L;
    private static final String REFRESH_TOKEN = "refresh-token-value";
    private static final LocalDateTime EXPIRES_AT = LocalDateTime.of(2026, 12, 31, 23, 59, 59);

    private AuthTokenRepositoryImpl authTokenRepository;

    @Mock
    private AdminTokenMapper adminTokenMapper;

    @BeforeEach
    void setUp() {
        authTokenRepository = new AuthTokenRepositoryImpl();
        ReflectionTestUtils.setField(authTokenRepository, "adminTokenMapper", adminTokenMapper);
        ReflectionTestUtils.setField(authTokenRepository, "adminTokenDataConverter", new AdminTokenDataConverter());
    }

    @Test
    @DisplayName("saveRefreshToken fails when refresh token is null")
    void saveRefreshToken_nullRefreshToken_fails() {
        ParamException exception = assertThrows(
                ParamException.class,
                () -> authTokenRepository.saveRefreshToken(null)
        );

        assertEquals(ParamErrorCode.NULL_PARAM, exception.getErrorType());
        verifyNoInteractions(adminTokenMapper);
    }

    @Test
    @DisplayName("saveRefreshToken maps JwtToken to AdminTokenDO and inserts successfully")
    void saveRefreshToken_validRefreshToken_mapsAndInserts() {
        JwtToken refreshToken = buildJwtToken();
        when(adminTokenMapper.insert(org.mockito.ArgumentMatchers.any(AdminTokenDO.class))).thenReturn(1);

        authTokenRepository.saveRefreshToken(refreshToken);

        ArgumentCaptor<AdminTokenDO> adminTokenCaptor = ArgumentCaptor.forClass(AdminTokenDO.class);
        verify(adminTokenMapper).insert(adminTokenCaptor.capture());
        AdminTokenDO actualAdminTokenDO = adminTokenCaptor.getValue();
        assertEquals(TOKEN_ID, actualAdminTokenDO.getId());
        assertEquals(ADMIN_ID, actualAdminTokenDO.getAdminId());
        assertEquals(REFRESH_TOKEN, actualAdminTokenDO.getRefreshToken());
        assertFalse(actualAdminTokenDO.getRevoked());
        assertEquals(EXPIRES_AT, actualAdminTokenDO.getExpiresAt());
        assertNotNull(actualAdminTokenDO.getCreatedAt());
    }

    @Test
    @DisplayName("saveRefreshToken returns nothing when insertion succeeds")
    void saveRefreshToken_successfulInsertion_returnsNothing() {
        JwtToken refreshToken = buildJwtToken();
        when(adminTokenMapper.insert(org.mockito.ArgumentMatchers.any(AdminTokenDO.class))).thenReturn(1);

        authTokenRepository.saveRefreshToken(refreshToken);

        verify(adminTokenMapper).insert(org.mockito.ArgumentMatchers.any(AdminTokenDO.class));
    }

    @Test
    @DisplayName("saveRefreshToken fails when insertion affects zero rows")
    void saveRefreshToken_zeroRowsInserted_fails() {
        JwtToken refreshToken = buildJwtToken();
        when(adminTokenMapper.insert(org.mockito.ArgumentMatchers.any(AdminTokenDO.class))).thenReturn(0);

        DependencyException exception = assertThrows(
                DependencyException.class,
                () -> authTokenRepository.saveRefreshToken(refreshToken)
        );

        assertEquals(DependencyErrorCode.DATABASE_INSERT_FAILED, exception.getErrorType());
    }

    @Test
    @DisplayName("saveRefreshToken fails when mapper throws")
    void saveRefreshToken_mapperThrows_fails() {
        JwtToken refreshToken = buildJwtToken();
        when(adminTokenMapper.insert(org.mockito.ArgumentMatchers.any(AdminTokenDO.class)))
                .thenThrow(new RuntimeException("db failure"));

        DependencyException exception = assertThrows(
                DependencyException.class,
                () -> authTokenRepository.saveRefreshToken(refreshToken)
        );

        assertEquals(DependencyErrorCode.DATABASE_INSERT_FAILED, exception.getErrorType());
    }

    @Test
    @DisplayName("queryByRefreshToken fails when refresh token is null")
    void queryByRefreshToken_nullRefreshToken_fails() {
        ParamException exception = assertThrows(
                ParamException.class,
                () -> authTokenRepository.queryByRefreshToken(null)
        );

        assertEquals(ParamErrorCode.NULL_PARAM, exception.getErrorType());
        verifyNoInteractions(adminTokenMapper);
    }

    @Test
    @DisplayName("queryByRefreshToken returns JwtToken when mapper finds token")
    void queryByRefreshToken_tokenFound_returnsJwtToken() {
        AdminTokenDO adminTokenDO = buildAdminTokenDO();
        when(adminTokenMapper.selectByRefreshToken(REFRESH_TOKEN)).thenReturn(adminTokenDO);

        JwtToken actualToken = authTokenRepository.queryByRefreshToken(REFRESH_TOKEN);

        assertNotNull(actualToken);
        assertEquals(TOKEN_ID, actualToken.getId());
        assertEquals(ADMIN_ID, actualToken.getAdminId().getId());
        assertEquals(REFRESH_TOKEN, actualToken.getToken());
        assertNull(actualToken.getTokenHash());
        assertEquals(EXPIRES_AT, actualToken.getExpiresAt());
        assertFalse(actualToken.isRevoked());
    }

    @Test
    @DisplayName("queryByRefreshToken returns null when token is not found")
    void queryByRefreshToken_tokenNotFound_returnsNull() {
        when(adminTokenMapper.selectByRefreshToken(REFRESH_TOKEN)).thenReturn(null);

        JwtToken actualToken = authTokenRepository.queryByRefreshToken(REFRESH_TOKEN);

        assertNull(actualToken);
        verify(adminTokenMapper).selectByRefreshToken(REFRESH_TOKEN);
    }

    @Test
    @DisplayName("queryByRefreshToken wraps mapper failure as dependency exception")
    void queryByRefreshToken_mapperThrows_fails() {
        when(adminTokenMapper.selectByRefreshToken(REFRESH_TOKEN)).thenThrow(new RuntimeException("db failure"));

        DependencyException exception = assertThrows(
                DependencyException.class,
                () -> authTokenRepository.queryByRefreshToken(REFRESH_TOKEN)
        );

        assertEquals(DependencyErrorCode.DATABASE_QUERY_FAILED, exception.getErrorType());
    }

    @Test
    @DisplayName("queryByRefreshToken preserves revoked flag for revoked token")
    void queryByRefreshToken_revokedToken_preservesRevokedFlag() {
        AdminTokenDO adminTokenDO = buildAdminTokenDO();
        adminTokenDO.setRevoked(true);
        when(adminTokenMapper.selectByRefreshToken(REFRESH_TOKEN)).thenReturn(adminTokenDO);

        JwtToken actualToken = authTokenRepository.queryByRefreshToken(REFRESH_TOKEN);

        assertTrue(actualToken.isRevoked());
        verify(adminTokenMapper).selectByRefreshToken(REFRESH_TOKEN);
    }

    private JwtToken buildJwtToken() {
        return new JwtToken(TOKEN_ID, new AdminId(ADMIN_ID), REFRESH_TOKEN, "hash", EXPIRES_AT, false);
    }

    private AdminTokenDO buildAdminTokenDO() {
        AdminTokenDO adminTokenDO = new AdminTokenDO();
        adminTokenDO.setId(TOKEN_ID);
        adminTokenDO.setAdminId(ADMIN_ID);
        adminTokenDO.setRefreshToken(REFRESH_TOKEN);
        adminTokenDO.setRevoked(false);
        adminTokenDO.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        adminTokenDO.setExpiresAt(EXPIRES_AT);
        return adminTokenDO;
    }
}
