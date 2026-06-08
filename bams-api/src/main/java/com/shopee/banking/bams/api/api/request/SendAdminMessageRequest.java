package com.shopee.banking.bams.api.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class SendAdminMessageRequest extends BaseAppRequest {

    @NotNull
    @Positive
    private Long adminId;
}
