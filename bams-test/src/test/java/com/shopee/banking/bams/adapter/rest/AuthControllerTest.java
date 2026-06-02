package com.shopee.banking.bams.adapter.rest;

import com.shopee.banking.bams.api.api.request.LoginRequest;
import com.shopee.banking.bams.api.api.request.RefreshTokenRequest;
import com.shopee.banking.bams.api.api.response.LoginResponse;
import com.shopee.banking.bams.api.api.response.RefreshTokenResponse;
import com.shopee.banking.bams.app.service.IAuthService;
import com.shopee.banking.bams.app.service.dto.AuthTokens;
import com.shopee.banking.bams.common.exception.ParamException;
import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;
import com.shopee.banking.bams.common.result.Result;
import com.shopee.banking.bams.domain.aggregateRoot.JwtToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final String USERNAME = "test-admin";
    private static final String PASSWORD = "P@ssw0rd!";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";

    private AuthController authController;

    @Mock
    private IAuthService authService;

    @BeforeEach
    void setUp() {
        authController = new AuthController();
        ReflectionTestUtils.setField(authController, "authService", authService);
    }

    @Test
    @DisplayName("login succeeds with valid username and password")
    void login_validRequest_succeeds() {
        AuthTokens authTokens = new AuthTokens();
        authTokens.setAccessToken(new JwtToken(ACCESS_TOKEN));
        authTokens.setRefreshToken(new JwtToken(REFRESH_TOKEN));
        when(authService.login(USERNAME, PASSWORD)).thenReturn(authTokens);

        Result<LoginResponse> result = authController.login(buildLoginRequest(USERNAME, PASSWORD));

        assertEquals(Result.SUCCESS_CODE, result.getCode());
        assertEquals(Result.SUCCESS_MSG, result.getMsg());
        assertNotNull(result.getData());
        assertEquals(ACCESS_TOKEN, result.getData().getAccessToken());
        assertEquals(REFRESH_TOKEN, result.getData().getRefreshToken());
        verify(authService).login(USERNAME, PASSWORD);
    }

    @Test
    @DisplayName("login fails when username is missing")
    void login_missingUsername_throwsInvalidParam() {
        ParamException exception = assertThrows(
                ParamException.class,
                () -> authController.login(buildLoginRequest(null, PASSWORD))
        );

        assertEquals(ParamErrorCode.INVALID_PARAM, exception.getErrorType());
        assertTrue(exception.getMessage().contains("username"));
        verify(authService, never()).login(USERNAME, PASSWORD);
    }

    @Test
    @DisplayName("login fails when password is missing")
    void login_missingPassword_throwsInvalidParam() {
        ParamException exception = assertThrows(
                ParamException.class,
                () -> authController.login(buildLoginRequest(USERNAME, null))
        );

        assertEquals(ParamErrorCode.INVALID_PARAM, exception.getErrorType());
        assertTrue(exception.getMessage().contains("password"));
        verify(authService, never()).login(USERNAME, PASSWORD);
    }

    @Test
    @DisplayName("refresh succeeds with valid refresh token")
    void refresh_validRequest_succeeds() {
        when(authService.refresh(REFRESH_TOKEN)).thenReturn(new JwtToken(ACCESS_TOKEN));

        Result<RefreshTokenResponse> result = authController.refresh(buildRefreshTokenRequest(REFRESH_TOKEN));

        assertEquals(Result.SUCCESS_CODE, result.getCode());
        assertEquals(Result.SUCCESS_MSG, result.getMsg());
        assertNotNull(result.getData());
        assertEquals(ACCESS_TOKEN, result.getData().getAccessToken());
        verify(authService).refresh(REFRESH_TOKEN);
    }

    @Test
    @DisplayName("refresh fails when refresh token is blank")
    void refresh_blankToken_throwsInvalidParam() {
        ParamException exception = assertThrows(
                ParamException.class,
                () -> authController.refresh(buildRefreshTokenRequest("   "))
        );

        assertEquals(ParamErrorCode.INVALID_PARAM, exception.getErrorType());
        assertTrue(exception.getMessage().contains("refreshToken"));
        verify(authService, never()).refresh(REFRESH_TOKEN);
    }

    @Test
    @DisplayName("refresh fails when refresh token is missing")
    void refresh_missingToken_throwsInvalidParam() {
        ParamException exception = assertThrows(
                ParamException.class,
                () -> authController.refresh(buildRefreshTokenRequest(null))
        );

        assertEquals(ParamErrorCode.INVALID_PARAM, exception.getErrorType());
        assertTrue(exception.getMessage().contains("refreshToken"));
        verify(authService, never()).refresh(REFRESH_TOKEN);
    }

    private LoginRequest buildLoginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.username = username;
        request.password = password;
        return request;
    }

    private RefreshTokenRequest buildRefreshTokenRequest(String refreshToken) {
        RefreshTokenRequest request = new RefreshTokenRequest();
        ReflectionTestUtils.setField(request, "refreshToken", refreshToken);
        return request;
    }
}
