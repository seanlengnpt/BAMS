package com.shopee.banking.bams.api.api.request;

import jakarta.validation.constraints.NotBlank;

public class ViewAdminProfileRequest extends BaseAppRequest{

    @NotBlank(message="Admin Id is required")
    public String id;
}
