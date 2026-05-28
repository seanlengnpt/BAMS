package com.shopee.banking.bams.api.api.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class EditAdminProfileRequest extends BaseAppRequest {

    @Size(max = 50, message = "Nickname must not exceed 50 characters")
    private String nickname;

    @Size(max = 250, message = "Profile picture URL must not exceed 250 characters")
    private String profilePictureUrl;

    @PositiveOrZero
    private Long id;
}
