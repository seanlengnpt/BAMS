package com.shopee.banking.bams.infra.dal.repository.impl;

import com.shopee.banking.bams.common.ParamException;
import com.shopee.banking.bams.common.enums.ParamErrorCode;
import com.shopee.banking.bams.domain.aggregateRoot.Admin;
import com.shopee.banking.bams.domain.valueObject.AdminId;
import com.shopee.banking.bams.infra.dal.converter.AdminDataConverter;
import com.shopee.banking.bams.infra.dal.dataObject.AdminDO;
import com.shopee.banking.bams.infra.dal.mapper.AdminMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminRepositoryImplTest {

    private static final Long ADMIN_ID = 1L;
    private static final String USERNAME = "test-admin";
    private static final String PASSWORD = "hashed-password";
    private static final String NICKNAME = "test-nickname";
    private static final String PROFILE_PICTURE_URL = "https://example.com/profile.png";

    private AdminRepositoryImpl adminRepository;

    @Mock
    private AdminMapper adminMapper;

    @Mock
    private AdminDataConverter adminDataConverter;

    @BeforeEach
    void setUp() {
        adminRepository = new AdminRepositoryImpl();
        ReflectionTestUtils.setField(adminRepository, "adminMapper", adminMapper);
        ReflectionTestUtils.setField(adminRepository, "adminDataConverter", adminDataConverter);
    }

    @Test
    @DisplayName("queryById returns admin when id is valid")
    void queryById_validAdminId_returnsAdmin() {
        AdminId adminId = buildAdminId();
        AdminDO adminDO = buildAdminDO();
        Admin expectedAdmin = buildAdmin();
        when(adminMapper.selectById(adminId.getId())).thenReturn(adminDO);
        when(adminDataConverter.toEntity(adminDO)).thenReturn(expectedAdmin);

        Admin actualAdmin = adminRepository.queryById(adminId);

        assertSame(expectedAdmin, actualAdmin);
        verify(adminMapper, times(1)).selectById(adminId.getId());
        verify(adminDataConverter, times(1)).toEntity(adminDO);
    }

    @Test
    @DisplayName("queryById fails when admin id is null")
    void queryById_nullAdminId_fails() {
        ParamException exception = assertThrows(ParamException.class, () -> adminRepository.queryById(null));

        assertEquals(ParamErrorCode.NULL_PARAM, exception.getErrorType());
        verifyNoInteractions(adminMapper);
        verifyNoInteractions(adminDataConverter);
    }

    @Test
    @DisplayName("queryByUsername returns admin when username is valid")
    void queryByUsername_validUsername_returnsAdmin() {
        AdminDO adminDO = buildAdminDO();
        Admin expectedAdmin = buildAdmin();
        when(adminMapper.selectByUsername(USERNAME)).thenReturn(adminDO);
        when(adminDataConverter.toEntity(adminDO)).thenReturn(expectedAdmin);

        Admin actualAdmin = adminRepository.queryByUsername(USERNAME);

        assertSame(expectedAdmin, actualAdmin);
        verify(adminMapper, times(1)).selectByUsername(USERNAME);
        verify(adminDataConverter, times(1)).toEntity(adminDO);
    }

    @Test
    @DisplayName("queryByUsername fails when username is null")
    void queryByUsername_nullUsername_fails() {
        ParamException exception = assertThrows(ParamException.class, () -> adminRepository.queryByUsername(null));

        assertEquals(ParamErrorCode.NULL_PARAM, exception.getErrorType());
        verifyNoInteractions(adminMapper);
        verifyNoInteractions(adminDataConverter);
    }

    @Test
    @DisplayName("updateProfile returns mapper result when id, nickname, and profilePictureUrl are valid")
    void updateProfile_validAdminIdNicknameAndProfilePictureUrl_returnsMapperResult() {
        AdminId adminId = buildAdminId();
        when(adminMapper.updateProfile(adminId.getId(), NICKNAME, PROFILE_PICTURE_URL)).thenReturn(1);

        int actualResult = adminRepository.updateProfile(adminId, NICKNAME, PROFILE_PICTURE_URL);

        assertEquals(1, actualResult);
        verify(adminMapper, times(1)).updateProfile(adminId.getId(), NICKNAME, PROFILE_PICTURE_URL);
    }

    @Test
    @DisplayName("updateProfile returns mapper result when nickname only")
    void updateProfile_nicknameOnly_returnsMapperResult() {
        AdminId adminId = buildAdminId();
        when(adminMapper.updateProfile(adminId.getId(), NICKNAME, null)).thenReturn(1);

        int actualResult = adminRepository.updateProfile(adminId, NICKNAME, null);

        assertEquals(1, actualResult);
        verify(adminMapper, times(1)).updateProfile(adminId.getId(), NICKNAME, null);
    }

    @Test
    @DisplayName("updateProfile returns mapper result when profilePictureUrl only")
    void updateProfile_profilePictureUrlOnly_returnsMapperResult() {
        AdminId adminId = buildAdminId();
        when(adminMapper.updateProfile(adminId.getId(), null, PROFILE_PICTURE_URL)).thenReturn(1);

        int actualResult = adminRepository.updateProfile(adminId, null, PROFILE_PICTURE_URL);

        assertEquals(1, actualResult);
        verify(adminMapper, times(1)).updateProfile(adminId.getId(), null, PROFILE_PICTURE_URL);
    }

    @Test
    @DisplayName("updateProfile fails when admin id is null")
    void updateProfile_nullAdminId_fails() {
        ParamException exception = assertThrows(
                ParamException.class,
                () -> adminRepository.updateProfile(null, NICKNAME, PROFILE_PICTURE_URL)
        );

        assertEquals(ParamErrorCode.NULL_PARAM, exception.getErrorType());
        verifyNoInteractions(adminMapper);
        verifyNoInteractions(adminDataConverter);
    }

    @Test
    @DisplayName("updateProfile returns zero when mapper updates zero rows")
    void updateProfile_mapperReturnsZero_returnsZero() {
        AdminId adminId = buildAdminId();
        when(adminMapper.updateProfile(adminId.getId(), NICKNAME, PROFILE_PICTURE_URL)).thenReturn(0);

        int actualResult = adminRepository.updateProfile(adminId, NICKNAME, PROFILE_PICTURE_URL);

        assertEquals(0, actualResult);
        verify(adminMapper, times(1)).updateProfile(adminId.getId(), NICKNAME, PROFILE_PICTURE_URL);
    }

    private AdminId buildAdminId() {
        return new AdminId(ADMIN_ID);
    }

    private AdminDO buildAdminDO() {
        return buildAdminDO(ADMIN_ID, USERNAME, PASSWORD, NICKNAME, PROFILE_PICTURE_URL);
    }

    private AdminDO buildAdminDO(Long id,
                                 String username,
                                 String password,
                                 String nickname,
                                 String profilePictureUrl) {
        AdminDO adminDO = new AdminDO();
        adminDO.setId(id);
        adminDO.setUsername(username);
        adminDO.setPassword(password);
        adminDO.setNickname(nickname);
        adminDO.setProfilePictureUrl(profilePictureUrl);
        return adminDO;
    }

    private Admin buildAdmin() {
        return buildAdmin(ADMIN_ID, PASSWORD, USERNAME, NICKNAME, PROFILE_PICTURE_URL);
    }

    private Admin buildAdmin(Long id,
                             String hashedPassword,
                             String username,
                             String nickname,
                             String profilePictureUrl) {
        return new Admin(id, hashedPassword, username, nickname, profilePictureUrl);
    }
}
