package com.shopee.banking.bams.app.service.impl;

import com.shopee.banking.bams.app.service.dto.query.AdminProfileQuery;
import com.shopee.banking.bams.app.service.dto.query.EditAdminProfileQuery;
import com.shopee.banking.bams.common.exception.BizException;
import com.shopee.banking.bams.common.exception.ParamException;
import com.shopee.banking.bams.common.exception.enums.BizErrorCode;
import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;
import com.shopee.banking.bams.domain.aggregateRoot.Admin;
import com.shopee.banking.bams.domain.repository.IAdminRepository;
import com.shopee.banking.bams.domain.valueObject.AdminId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    private static final Long ADMIN_ID = 1L;
    private static final String USERNAME = "test-admin";
    private static final String NICKNAME = "test-nickname";
    private static final String PROFILE_PICTURE_URL = "https://example.com/profile.png";

    private AdminServiceImpl adminService;

    @Mock
    private IAdminRepository adminRepository;

    @BeforeEach
    void setUp() {
        adminService = new AdminServiceImpl();
        ReflectionTestUtils.setField(adminService, "adminRepository", adminRepository);
    }

    @Test
    @DisplayName("getAdminById returns admin when repository finds a match")
    void getAdminById_adminExists_returnsAdmin() {
        AdminProfileQuery query = buildAdminProfileQuery(ADMIN_ID);
        Admin admin = buildAdmin();
        when(adminRepository.queryById(query.getAdminId())).thenReturn(admin);

        Admin result = adminService.getAdminById(query);

        assertEquals(admin, result);
        verify(adminRepository).queryById(query.getAdminId());
    }

    @Test
    @DisplayName("getAdminById fails when repository returns null")
    void getAdminById_adminDoesNotExist_fails() {
        AdminProfileQuery query = buildAdminProfileQuery(999L);
        when(adminRepository.queryById(query.getAdminId())).thenReturn(null);

        BizException exception = assertThrows(BizException.class, () -> adminService.getAdminById(query));

        assertEquals(BizErrorCode.ADMIN_NOT_FOUND_MAPPING, exception.getErrorType());
        verify(adminRepository).queryById(query.getAdminId());
    }

    @Test
    @DisplayName("editAdminProfile succeeds when admin exists and update affects one row")
    void editAdminProfile_adminExistsAndUpdateReturnsOne_succeeds() {
        EditAdminProfileQuery query = buildEditAdminProfileQuery();
        AdminId adminId = query.getAdminId();
        when(adminRepository.queryById(adminId)).thenReturn(buildAdmin());
        when(adminRepository.updateProfile(adminId, NICKNAME, PROFILE_PICTURE_URL)).thenReturn(1);

        assertDoesNotThrow(() -> adminService.editAdminProfile(query));

        InOrder inOrder = inOrder(adminRepository);
        inOrder.verify(adminRepository).queryById(adminId);
        inOrder.verify(adminRepository).updateProfile(adminId, NICKNAME, PROFILE_PICTURE_URL);
        verify(adminRepository, times(1)).updateProfile(adminId, NICKNAME, PROFILE_PICTURE_URL);
    }

    @Test
    @DisplayName("editAdminProfile fails when admin does not exist")
    void editAdminProfile_adminDoesNotExist_fails() {
        EditAdminProfileQuery query = buildEditAdminProfileQuery();
        when(adminRepository.queryById(query.getAdminId())).thenReturn(null);

        BizException exception = assertThrows(BizException.class, () -> adminService.editAdminProfile(query));

        assertEquals(BizErrorCode.ADMIN_NOT_FOUND_MAPPING, exception.getErrorType());
        verify(adminRepository, never()).updateProfile(any(AdminId.class), any(), any());
    }

    @Test
    @DisplayName("editAdminProfile fails when update affects zero rows")
    void editAdminProfile_updateReturnsZero_fails() {
        EditAdminProfileQuery query = buildEditAdminProfileQuery();
        AdminId adminId = query.getAdminId();
        when(adminRepository.queryById(adminId)).thenReturn(buildAdmin());
        when(adminRepository.updateProfile(adminId, NICKNAME, PROFILE_PICTURE_URL)).thenReturn(0);

        ParamException exception = assertThrows(ParamException.class, () -> adminService.editAdminProfile(query));

        assertEquals(ParamErrorCode.INVALID_PARAM, exception.getErrorType());
        verify(adminRepository, times(1)).updateProfile(adminId, NICKNAME, PROFILE_PICTURE_URL);
    }

//    @Test
//    @DisplayName("editAdminProfile fails when update affects more than one row")
//    void editAdminProfile_updateReturnsMoreThanOne_fails() {
//        EditAdminProfileQuery query = buildEditAdminProfileQuery();
//        AdminId adminId = query.getAdminId();
//        when(adminRepository.queryById(adminId)).thenReturn(buildAdmin());
//        when(adminRepository.updateProfile(adminId, NICKNAME, PROFILE_PICTURE_URL)).thenReturn(2);
//
//        ParamException exception = assertThrows(ParamException.class, () -> adminService.editAdminProfile(query));
//
//        assertEquals(ParamErrorCode.INVALID_PARAM, exception.getErrorType());
//        verify(adminRepository, times(1)).updateProfile(adminId, NICKNAME, PROFILE_PICTURE_URL);
//    }

    @Test
    @DisplayName("editAdminProfile succeeds with nickname only")
    void editAdminProfile_nicknameOnly_succeeds() {
        EditAdminProfileQuery query = buildEditAdminProfileQuery(buildAdminId(), NICKNAME, null);
        AdminId adminId = query.getAdminId();

        when(adminRepository.queryById(adminId)).thenReturn(buildAdmin());
        when(adminRepository.updateProfile(adminId, NICKNAME, null)).thenReturn(1);

        assertDoesNotThrow(() -> adminService.editAdminProfile(query));

        InOrder inOrder = inOrder(adminRepository);
        inOrder.verify(adminRepository).queryById(adminId);
        inOrder.verify(adminRepository).updateProfile(adminId, NICKNAME, null);
    }

    @Test
    @DisplayName("editAdminProfile succeeds with profilePictureUrl only")
    void editAdminProfile_profilePictureUrlOnly_succeeds() {
        EditAdminProfileQuery query = buildEditAdminProfileQuery(buildAdminId(), null, PROFILE_PICTURE_URL);
        AdminId adminId = query.getAdminId();
        when(adminRepository.queryById(adminId)).thenReturn(buildAdmin());
        when(adminRepository.updateProfile(adminId, null, PROFILE_PICTURE_URL)).thenReturn(1);

        assertDoesNotThrow(() -> adminService.editAdminProfile(query));

        InOrder inOrder = inOrder(adminRepository);
        inOrder.verify(adminRepository).queryById(adminId);
        inOrder.verify(adminRepository).updateProfile(adminId, null, PROFILE_PICTURE_URL);
    }

    private EditAdminProfileQuery buildEditAdminProfileQuery() {
        return buildEditAdminProfileQuery(buildAdminId(), NICKNAME, PROFILE_PICTURE_URL);
    }

    private AdminProfileQuery buildAdminProfileQuery(Long adminId) {
        AdminProfileQuery query = new AdminProfileQuery();
        query.setAdminId(new AdminId(adminId));
        return query;
    }

    private EditAdminProfileQuery buildEditAdminProfileQuery(AdminId adminId,
                                                             String nickname,
                                                             String profilePictureUrl) {
        EditAdminProfileQuery query = new EditAdminProfileQuery();
        query.setAdminId(adminId);
        query.setNickname(nickname);
        query.setProfilePictureUrl(profilePictureUrl);
        return query;
    }

    private AdminId buildAdminId() {
        return new AdminId(ADMIN_ID);
    }

    private Admin buildAdmin() {
        return buildAdmin(ADMIN_ID, USERNAME, NICKNAME, PROFILE_PICTURE_URL);
    }

    private Admin buildAdmin(Long id, String username, String nickname, String profilePictureUrl) {
        return new Admin(id, username, nickname, profilePictureUrl);
    }
}
