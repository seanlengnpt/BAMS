package com.shopee.banking.bams.api.api.response;

import com.shopee.banking.bams.common.enums.Gender;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ViewCustomerProfileResponse {
    private Long id;
    private String accountNumber;
    private String name;
    private Gender gender;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
