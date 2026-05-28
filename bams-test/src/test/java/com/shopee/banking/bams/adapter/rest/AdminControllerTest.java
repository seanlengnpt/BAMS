package com.shopee.banking.bams.adapter.rest;

import com.shopee.banking.bams.api.api.request.EditAdminProfileRequest;
import com.shopee.banking.bams.app.service.IAdminService;
import com.shopee.banking.bams.app.service.IAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private static final Long ADMIN_ID = 1L;
    private static final String NICKNAME = "test-nickname";
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
}
