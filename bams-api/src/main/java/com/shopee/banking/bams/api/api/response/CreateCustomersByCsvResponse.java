package com.shopee.banking.bams.api.api.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateCustomersByCsvResponse {
    private int modifiedCount;

    public CreateCustomersByCsvResponse(int modifiedCount) {
        this.modifiedCount = modifiedCount;
    }
}
