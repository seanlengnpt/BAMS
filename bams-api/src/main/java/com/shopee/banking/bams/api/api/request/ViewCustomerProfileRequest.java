package com.shopee.banking.bams.api.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class ViewCustomerProfileRequest extends BaseAppRequest{

    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Account number must be exactly 10 digits")
    String accNo;
}
