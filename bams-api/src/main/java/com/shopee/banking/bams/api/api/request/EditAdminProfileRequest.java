package com.shopee.banking.bams.api.api.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import org.hibernate.validator.constraints.URL;

@Getter
public class EditAdminProfileRequest extends BaseAppRequest {

    @Size(min=4, max = 50, message = "Nickname must not exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9 ]*$", message = "Nickname contains invalid characters")
    private String nickname;

    @URL
    @Size(min=1, max = 250, message = "Profile picture URL must not exceed 250 characters")
    private String profilePictureUrl;

    @Positive
    private Long id;
}
