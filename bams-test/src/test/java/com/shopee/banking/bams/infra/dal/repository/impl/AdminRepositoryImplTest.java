package com.shopee.banking.bams.infra.dal.repository.impl;

import com.shopee.banking.bams.domain.aggregateRoot.Admin;
import com.shopee.banking.bams.domain.valueObject.AdminId;
import com.shopee.banking.bams.infra.dal.converter.AdminDataConverter;
import com.shopee.banking.bams.infra.dal.dataObject.AdminDO;
import com.shopee.banking.bams.infra.dal.mapper.AdminMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
