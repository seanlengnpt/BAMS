package com.shopee.banking.bams.api.api.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateCustomersByCsvResponse {
    private int createdCount;
    private int modifiedCount;
    private List<String> errors;

    public CreateCustomersByCsvResponse(int createdCount, int modifiedCount, List<String> errors) {
        this.createdCount = createdCount;
        this.modifiedCount = modifiedCount;
        this.errors = errors;
    }
}
