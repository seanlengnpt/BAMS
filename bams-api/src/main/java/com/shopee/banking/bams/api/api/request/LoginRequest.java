package com.shopee.banking.bams.api.api.request;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest extends BaseAppRequest{

    @NotBlank(message="Username is required")
    public String username;

    @NotBlank(message="Password is required")
    public String password;
}
