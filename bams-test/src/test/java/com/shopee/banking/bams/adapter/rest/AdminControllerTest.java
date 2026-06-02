package com.shopee.banking.bams.adapter.rest;

import com.shopee.banking.bams.api.api.request.EditAdminProfileRequest;
import com.shopee.banking.bams.app.service.dto.query.EditAdminProfileQuery;
import com.shopee.banking.bams.app.service.IAdminService;
import com.shopee.banking.bams.app.service.IAuthService;
import com.shopee.banking.bams.common.ParamException;
import com.shopee.banking.bams.common.enums.ParamErrorCode;
import com.shopee.banking.bams.common.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private static final Long ADMIN_ID = 1L;
    private static final String NICKNAME = "test nickname";
    private static final String PROFILE_PICTURE_URL = "https://example.com/profile.png";

    private AdminController adminController;

    @Mock
    private IAdminService adminService;

    @Mock
    private IAuthService authService;

    @BeforeEach
    void setUp() {
        adminController = new AdminController();
        ReflectionTestUtils.setField(adminController, "adminService", adminService);
        ReflectionTestUtils.setField(adminController, "authService", authService);
    }

    @Test
    @DisplayName("editAdminProfile succeeds with valid id, nickname, and profilePictureUrl")
    void editAdminProfile_validIdNicknameAndProfilePictureUrl_succeeds() {
        Result<?> result = adminController.editAdminProfile(buildEditAdminProfileRequest());
        assertSuccess(result);
        EditAdminProfileQuery query = captureEditAdminProfileQuery();
        assertEquals(ADMIN_ID, query.getAdminId().getId());
        assertEquals(NICKNAME, query.getNickname());
        assertEquals(PROFILE_PICTURE_URL, query.getProfilePictureUrl());
    }

    @Test
    @DisplayName("editAdminProfile succeeds with valid id and nickname only")
    void editAdminProfile_validIdAndNicknameOnly_succeeds() {
        EditAdminProfileRequest request = buildEditAdminProfileRequest(ADMIN_ID, NICKNAME, null);

        Result<?> result = adminController.editAdminProfile(request);

        assertSuccess(result);
        EditAdminProfileQuery query = captureEditAdminProfileQuery();
        assertEquals(ADMIN_ID, query.getAdminId().getId());
        assertEquals(NICKNAME, query.getNickname());
        assertNull(query.getProfilePictureUrl());
    }

    @Test
    @DisplayName("editAdminProfile succeeds with valid id and profilePictureUrl only")
    void editAdminProfile_validIdAndProfilePictureUrlOnly_succeeds() {
        EditAdminProfileRequest request = buildEditAdminProfileRequest(ADMIN_ID, null, PROFILE_PICTURE_URL);

        Result<?> result = adminController.editAdminProfile(request);

        assertSuccess(result);
        EditAdminProfileQuery query = captureEditAdminProfileQuery();
        assertEquals(ADMIN_ID, query.getAdminId().getId());
        assertNull(query.getNickname());
        assertEquals(PROFILE_PICTURE_URL, query.getProfilePictureUrl());
    }

    @Test
    @DisplayName("editAdminProfile fails when nickname and profilePictureUrl are both null")
    void editAdminProfile_validIdAndNoEditableFields_fails() {
        EditAdminProfileRequest request = buildEditAdminProfileRequest(ADMIN_ID, null, null);

        ParamException exception = assertThrows(ParamException.class, () -> adminController.editAdminProfile(request));

        assertEquals(ParamErrorCode.NULL_PARAM, exception.getErrorType());
        verify(adminService, never()).editAdminProfile(any(EditAdminProfileQuery.class));
    }

    @Test
    @DisplayName("editAdminProfile fails when id is null")
    void editAdminProfile_nullId_fails() {
        EditAdminProfileRequest request = buildEditAdminProfileRequest(null, NICKNAME, PROFILE_PICTURE_URL);

        assertThrows(ParamException.class, () -> adminController.editAdminProfile(request));
        verify(adminService, never()).editAdminProfile(any(EditAdminProfileQuery.class));
    }

    @Test
    @DisplayName("editAdminProfile fails when id is negative")
    void editAdminProfile_negativeId_fails() {
        EditAdminProfileRequest request = buildEditAdminProfileRequest(-1L, NICKNAME, PROFILE_PICTURE_URL);

        assertThrows(ParamException.class, () -> adminController.editAdminProfile(request));
        verify(adminService, never()).editAdminProfile(any(EditAdminProfileQuery.class));
    }

    @Test
    @DisplayName("editAdminProfile delegates when id is zero")
    void editAdminProfile_zeroId_fails() {
        EditAdminProfileRequest request = buildEditAdminProfileRequest(0L, NICKNAME, PROFILE_PICTURE_URL);

        assertThrows(ParamException.class, () -> adminController.editAdminProfile(request));
        verify(adminService, never()).editAdminProfile(any(EditAdminProfileQuery.class));
    }

    @Test
    @DisplayName("editAdminProfile should fail when nickname is empty")
    void editAdminProfile_emptyNickname_shouldFail() {
        EditAdminProfileRequest request = buildEditAdminProfileRequest(ADMIN_ID, "", PROFILE_PICTURE_URL);

        assertThrows(ParamException.class, () -> adminController.editAdminProfile(request));
        verify(adminService, never()).editAdminProfile(any(EditAdminProfileQuery.class));
    }

    @Test
    @DisplayName("editAdminProfile should fail when nickname is blank")
    void editAdminProfile_blankNickname_shouldFail() {
        EditAdminProfileRequest request = buildEditAdminProfileRequest(ADMIN_ID, "      ", PROFILE_PICTURE_URL);

        assertThrows(ParamException.class, () -> adminController.editAdminProfile(request));
        verify(adminService, never()).editAdminProfile(any(EditAdminProfileQuery.class));
    }

    @Test
    @DisplayName("editAdminProfile should fail when nickname has invalid characters")
    void editAdminProfile_invalidNicknameCharacters_shouldFail() {
        EditAdminProfileRequest request = buildEditAdminProfileRequest(ADMIN_ID, "John@#$", PROFILE_PICTURE_URL);

        assertThrows(ParamException.class, () -> adminController.editAdminProfile(request));
        verify(adminService, never()).editAdminProfile(any(EditAdminProfileQuery.class));
    }

    @Test
    @DisplayName("editAdminProfile fails when nickname is longer than 50 characters")
    void editAdminProfile_nicknameLongerThan50_fails() {
        EditAdminProfileRequest request = buildEditAdminProfileRequest(ADMIN_ID, "a".repeat(51), PROFILE_PICTURE_URL);

        assertThrows(ParamException.class, () -> adminController.editAdminProfile(request));
        verify(adminService, never()).editAdminProfile(any(EditAdminProfileQuery.class));
    }

    @Test
    @DisplayName("editAdminProfile succeeds when nickname is exactly 50 characters")
    void editAdminProfile_nicknameExactly50_succeeds() {
        String nickname = "a".repeat(50);
        EditAdminProfileRequest request = buildEditAdminProfileRequest(ADMIN_ID, nickname, PROFILE_PICTURE_URL);

        Result<?> result = adminController.editAdminProfile(request);

        assertSuccess(result);
        EditAdminProfileQuery query = captureEditAdminProfileQuery();
        assertEquals(50, query.getNickname().length());
        assertEquals(nickname, query.getNickname());
    }

    @Test
    @DisplayName("editAdminProfile should fail when profilePictureUrl is empty")
    void editAdminProfile_emptyProfilePictureUrl_shouldFail() {
        EditAdminProfileRequest request = buildEditAdminProfileRequest(ADMIN_ID, NICKNAME, "");

        assertThrows(ParamException.class, () -> adminController.editAdminProfile(request));
        verify(adminService, never()).editAdminProfile(any(EditAdminProfileQuery.class));
    }

    @Test
    @DisplayName("editAdminProfile should fail when profilePictureUrl is blank")
    void editAdminProfile_blankProfilePictureUrl_shouldFail() {
        EditAdminProfileRequest request = buildEditAdminProfileRequest(ADMIN_ID, NICKNAME, "   ");

        assertThrows(ParamException.class, () -> adminController.editAdminProfile(request));
        verify(adminService, never()).editAdminProfile(any(EditAdminProfileQuery.class));
    }

    @Test
    @DisplayName("editAdminProfile should fail when profilePictureUrl is malformed")
    void editAdminProfile_malformedProfilePictureUrl_shouldFail() {
        EditAdminProfileRequest request = buildEditAdminProfileRequest(ADMIN_ID, NICKNAME, "not-a-url");

        assertThrows(ParamException.class, () -> adminController.editAdminProfile(request));
        verify(adminService, never()).editAdminProfile(any(EditAdminProfileQuery.class));
    }

    @Test
    @DisplayName("editAdminProfile should fail when profilePictureUrl uses an unsupported scheme")
    void editAdminProfile_unsupportedProfilePictureUrlScheme_shouldFail() {
        EditAdminProfileRequest request = buildEditAdminProfileRequest(ADMIN_ID, NICKNAME, "javascript:alert(1)");

        assertThrows(ParamException.class, () -> adminController.editAdminProfile(request));
        verify(adminService, never()).editAdminProfile(any(EditAdminProfileQuery.class));
    }

    @Test
    @DisplayName("editAdminProfile fails when profilePictureUrl is longer than 250 characters")
    void editAdminProfile_profilePictureUrlLongerThan250_fails() {
        EditAdminProfileRequest request = buildEditAdminProfileRequest(ADMIN_ID, NICKNAME, buildProfilePictureUrlOfLength(251));

        assertThrows(ParamException.class, () -> adminController.editAdminProfile(request));
        verify(adminService, never()).editAdminProfile(any(EditAdminProfileQuery.class));
    }

    @Test
    @DisplayName("editAdminProfile succeeds when profilePictureUrl is exactly 250 characters")
    void editAdminProfile_profilePictureUrlExactly250_succeeds() {
        String profilePictureUrl = buildProfilePictureUrlOfLength(250);
        EditAdminProfileRequest request = buildEditAdminProfileRequest(ADMIN_ID, NICKNAME, profilePictureUrl);

        Result<?> result = adminController.editAdminProfile(request);

        assertSuccess(result);
        EditAdminProfileQuery query = captureEditAdminProfileQuery();
        assertEquals(250, query.getProfilePictureUrl().length());
        assertEquals(profilePictureUrl, query.getProfilePictureUrl());
    }

    private EditAdminProfileRequest buildEditAdminProfileRequest() {
        return buildEditAdminProfileRequest(ADMIN_ID, NICKNAME, PROFILE_PICTURE_URL);
    }

    private EditAdminProfileRequest buildEditAdminProfileRequest(Long id,
                                                                 String nickname,
                                                                 String profilePictureUrl) {
        EditAdminProfileRequest request = new EditAdminProfileRequest();
        ReflectionTestUtils.setField(request, "id", id);
        ReflectionTestUtils.setField(request, "nickname", nickname);
        ReflectionTestUtils.setField(request, "profilePictureUrl", profilePictureUrl);
        return request;
    }

    private void assertSuccess(Result<?> result) {
        assertEquals(Result.SUCCESS_CODE, result.getCode());
        assertEquals(Result.SUCCESS_MSG, result.getMsg());
        assertNull(result.getData());
    }

    private EditAdminProfileQuery captureEditAdminProfileQuery() {
        ArgumentCaptor<EditAdminProfileQuery> queryCaptor = ArgumentCaptor.forClass(EditAdminProfileQuery.class);
        verify(adminService).editAdminProfile(queryCaptor.capture());
        return queryCaptor.getValue();
    }

    private String buildProfilePictureUrlOfLength(int length) {
        String prefix = "https://example.com/";
        String suffix = ".jpg";
        return prefix + "a".repeat(length - prefix.length() - suffix.length()) + suffix;
    }
}
