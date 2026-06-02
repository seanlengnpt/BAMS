package com.shopee.banking.bams.api.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateCustomersByCsvRequest extends BaseAppRequest {

    @NotBlank(message = "CSV file path is required")
    private String csvFilePath;
}
