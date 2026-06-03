package com.shopee.banking.bams.adapter.rest;

import com.shopee.banking.bams.api.api.request.EditAdminProfileRequest;
import com.shopee.banking.bams.api.api.request.ViewAdminProfileRequest;
import com.shopee.banking.bams.api.api.response.EditAdminProfileResponse;
import com.shopee.banking.bams.api.api.response.ViewAdminProfileResponse;
import com.shopee.banking.bams.app.service.IAdminService;
import com.shopee.banking.bams.app.service.dto.query.AdminProfileQuery;
import com.shopee.banking.bams.app.service.dto.query.EditAdminProfileQuery;
import com.shopee.banking.bams.common.exception.BizException;
import com.shopee.banking.bams.common.exception.ParamException;
import com.shopee.banking.bams.common.exception.enums.BizErrorCode;
import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;
import com.shopee.banking.bams.common.result.Result;
import com.shopee.banking.bams.domain.aggregateRoot.Admin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private static final Long ADMIN_ID = 1L;
    private static final Long VERSION = 0L;
    private static final String NICKNAME = "test nickname";
    private static final String PROFILE_PICTURE_URL = "https://example.com/profile.png";
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2020, 5, 1, 8, 30);
    private static final LocalDateTime MODIFIED_AT = LocalDateTime.of(2020, 5, 2, 9, 45);

    private AdminController adminController;

    @Mock
    private IAdminService adminService;


    @BeforeEach
    void setUp() {
        adminController = new AdminController();
        ReflectionTestUtils.setField(adminController, "adminService", adminService);
    }

    @Test
    @DisplayName("getAdminProfile returns admin details when id exists")
    void getAdminProfile_validId_returnsAdminDetails() {
        when(adminService.getAdminById(any(AdminProfileQuery.class))).thenReturn(buildAdmin());

        Result<ViewAdminProfileResponse> result = adminController.getAdminProfile(buildViewAdminProfileRequest("1"));

        assertEquals(Result.SUCCESS_CODE, result.getCode());
        assertEquals(Result.SUCCESS_MSG, result.getMsg());
        assertNotNull(result.getData());
        assertEquals(ADMIN_ID, result.getData().getId());
        assertEquals(VERSION, result.getData().getVersion());
        assertEquals("test-admin", result.getData().getUsername());
        assertEquals(NICKNAME, result.getData().getNickname());
        assertEquals(PROFILE_PICTURE_URL, result.getData().getProfilePictureUrl());
        assertEquals(CREATED_AT, result.getData().getCreatedAt());
        assertEquals(MODIFIED_AT, result.getData().getModifiedAt());

        AdminProfileQuery query = captureAdminProfileQuery();
        assertEquals(ADMIN_ID, query.getAdminId().getId());
    }

    @Test
    @DisplayName("getAdminProfile fails when id is blank")
    void getAdminProfile_blankId_fails() {
        ParamException exception = assertThrows(
                ParamException.class,
                () -> adminController.getAdminProfile(buildViewAdminProfileRequest("   "))
        );

        assertEquals(ParamErrorCode.INVALID_PARAM, exception.getErrorType());
        verify(adminService, never()).getAdminById(any(AdminProfileQuery.class));
    }

    @Test
    @DisplayName("getAdminProfile fails when id is invalid text")
    void getAdminProfile_nonNumericId_fails() {
        ParamException exception = assertThrows(
                ParamException.class,
                () -> adminController.getAdminProfile(buildViewAdminProfileRequest("abc"))
        );

        assertEquals(ParamErrorCode.INVALID_PARAM, exception.getErrorType());
        verify(adminService, never()).getAdminById(any(AdminProfileQuery.class));
    }

    @Test
    @DisplayName("getAdminProfile fails when admin id is not found")
    void getAdminProfile_unknownId_fails() {
        when(adminService.getAdminById(any(AdminProfileQuery.class)))
                .thenThrow(new BizException(BizErrorCode.ADMIN_NOT_FOUND_MAPPING, 999L));

        BizException exception = assertThrows(
                BizException.class,
                () -> adminController.getAdminProfile(buildViewAdminProfileRequest("999"))
        );

        assertEquals(BizErrorCode.ADMIN_NOT_FOUND_MAPPING, exception.getErrorType());
        AdminProfileQuery query = captureAdminProfileQuery();
        assertEquals(999L, query.getAdminId().getId());
    }

    @Test
    @DisplayName("editAdminProfile succeeds with valid id, nickname, and profilePictureUrl")
    void editAdminProfile_validIdNicknameAndProfilePictureUrl_succeeds() {
        when(adminService.editAdminProfile(any(EditAdminProfileQuery.class))).thenReturn(1);

        Result<EditAdminProfileResponse> result = adminController.editAdminProfile(buildEditAdminProfileRequest());
        assertSuccess(result);
        EditAdminProfileQuery query = captureEditAdminProfileQuery();
        assertEquals(ADMIN_ID, query.getAdminId().getId());
        assertEquals(NICKNAME, query.getNickname());
        assertEquals(PROFILE_PICTURE_URL, query.getProfilePictureUrl());
        assertEquals(VERSION, query.getVersion());
    }

    @Test
    @DisplayName("editAdminProfile succeeds with valid id and nickname only")
    void editAdminProfile_validIdAndNicknameOnly_succeeds() {
        EditAdminProfileRequest request = buildEditAdminProfileRequest(ADMIN_ID, NICKNAME, null);
        when(adminService.editAdminProfile(any(EditAdminProfileQuery.class))).thenReturn(1);

        Result<EditAdminProfileResponse> result = adminController.editAdminProfile(request);

        assertSuccess(result);
        EditAdminProfileQuery query = captureEditAdminProfileQuery();
        assertEquals(ADMIN_ID, query.getAdminId().getId());
        assertEquals(NICKNAME, query.getNickname());
        assertNull(query.getProfilePictureUrl());
        assertEquals(VERSION, query.getVersion());
    }

    @Test
    @DisplayName("editAdminProfile succeeds with valid id and profilePictureUrl only")
    void editAdminProfile_validIdAndProfilePictureUrlOnly_succeeds() {
        EditAdminProfileRequest request = buildEditAdminProfileRequest(ADMIN_ID, null, PROFILE_PICTURE_URL);
        when(adminService.editAdminProfile(any(EditAdminProfileQuery.class))).thenReturn(1);

        Result<EditAdminProfileResponse> result = adminController.editAdminProfile(request);

        assertSuccess(result);
        EditAdminProfileQuery query = captureEditAdminProfileQuery();
        assertEquals(ADMIN_ID, query.getAdminId().getId());
        assertNull(query.getNickname());
        assertEquals(PROFILE_PICTURE_URL, query.getProfilePictureUrl());
        assertEquals(VERSION, query.getVersion());
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
        when(adminService.editAdminProfile(any(EditAdminProfileQuery.class))).thenReturn(1);

        Result<EditAdminProfileResponse> result = adminController.editAdminProfile(request);

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
        when(adminService.editAdminProfile(any(EditAdminProfileQuery.class))).thenReturn(1);

        Result<EditAdminProfileResponse> result = adminController.editAdminProfile(request);

        assertSuccess(result);
        EditAdminProfileQuery query = captureEditAdminProfileQuery();
        assertEquals(250, query.getProfilePictureUrl().length());
        assertEquals(profilePictureUrl, query.getProfilePictureUrl());
    }

    private EditAdminProfileRequest buildEditAdminProfileRequest() {
        return buildEditAdminProfileRequest(ADMIN_ID, NICKNAME, PROFILE_PICTURE_URL);
    }

    private ViewAdminProfileRequest buildViewAdminProfileRequest(String id) {
        ViewAdminProfileRequest request = new ViewAdminProfileRequest();
        request.id = id;
        return request;
    }

    private EditAdminProfileRequest buildEditAdminProfileRequest(Long id,
                                                                 String nickname,
                                                                 String profilePictureUrl) {
        EditAdminProfileRequest request = new EditAdminProfileRequest();
        ReflectionTestUtils.setField(request, "id", id);
        ReflectionTestUtils.setField(request, "nickname", nickname);
        ReflectionTestUtils.setField(request, "profilePictureUrl", profilePictureUrl);
        ReflectionTestUtils.setField(request, "version", VERSION);
        return request;
    }

    private void assertSuccess(Result<EditAdminProfileResponse> result) {
        assertEquals(Result.SUCCESS_CODE, result.getCode());
        assertEquals(Result.SUCCESS_MSG, result.getMsg());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getModifiedCount());
    }

    private EditAdminProfileQuery captureEditAdminProfileQuery() {
        ArgumentCaptor<EditAdminProfileQuery> queryCaptor = ArgumentCaptor.forClass(EditAdminProfileQuery.class);
        verify(adminService).editAdminProfile(queryCaptor.capture());
        return queryCaptor.getValue();
    }

    private AdminProfileQuery captureAdminProfileQuery() {
        ArgumentCaptor<AdminProfileQuery> queryCaptor = ArgumentCaptor.forClass(AdminProfileQuery.class);
        verify(adminService).getAdminById(queryCaptor.capture());
        return queryCaptor.getValue();
    }

    private String buildProfilePictureUrlOfLength(int length) {
        String prefix = "https://example.com/";
        String suffix = ".jpg";
        return prefix + "a".repeat(length - prefix.length() - suffix.length()) + suffix;
    }

    private Admin buildAdmin() {
        Admin admin = Admin.builder()
                .id(ADMIN_ID)
                .username("test-admin")
                .adminNickname(NICKNAME)
                .adminProfilePictureUrl(PROFILE_PICTURE_URL)
                .createdAt(CREATED_AT)
                .modifiedAt(MODIFIED_AT)
                .build();
        admin.setVersion(VERSION);
        return admin;
    }
}
