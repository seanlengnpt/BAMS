package com.shopee.banking.bams.api.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RefreshTokenRequest extends BaseAppRequest{

    @NotBlank(message="Refresh token is required")
    private String refreshToken;

}
