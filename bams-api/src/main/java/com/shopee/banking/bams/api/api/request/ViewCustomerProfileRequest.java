package com.shopee.banking.bams.api.api.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ViewCustomerProfileRequest extends BaseAppRequest{

    @Size(min=10, max=10)
    String accNo;
}
