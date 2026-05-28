package com.shopee.banking.bams.api.api.response;

import com.shopee.banking.bams.api.api.enums.Gender;
import lombok.Setter;

@Setter
public class ViewCustomerProfileResponse {
    private Long id;
    private String accountNumber;
    private Gender gender;
}
