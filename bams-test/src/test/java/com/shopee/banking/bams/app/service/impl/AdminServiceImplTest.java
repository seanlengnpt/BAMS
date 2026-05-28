package com.shopee.banking.bams.app.service.impl;

import com.shopee.banking.bams.app.service.dto.query.EditAdminProfileQuery;
import com.shopee.banking.bams.domain.aggregateRoot.Admin;
import com.shopee.banking.bams.domain.repository.IAdminRepository;
import com.shopee.banking.bams.domain.valueObject.AdminId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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

    private EditAdminProfileQuery buildEditAdminProfileQuery() {
        return buildEditAdminProfileQuery(buildAdminId(), NICKNAME, PROFILE_PICTURE_URL);
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
