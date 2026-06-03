package com.shopee.banking.bams.api.api.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EditAdminProfileResponse {
    private int modifiedCount;

    public EditAdminProfileResponse(int modifiedCount) {
        this.modifiedCount = modifiedCount;
    }
}
