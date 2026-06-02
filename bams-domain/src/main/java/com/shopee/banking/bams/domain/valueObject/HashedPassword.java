package com.shopee.banking.bams.domain.valueObject;

import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;
import lombok.Getter;
import lombok.Setter;
import com.shopee.banking.bams.common.util.Asserter;

@Getter
@Setter
public class HashedPassword {
    private String hashedPassword;
    public HashedPassword(String hashedPassword){
        Asserter.assertNotNull(hashedPassword, ParamErrorCode.NULL_PARAM);
        this.hashedPassword = hashedPassword;
    }
}
