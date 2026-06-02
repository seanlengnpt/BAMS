package com.shopee.banking.bams.domain.valueObject;

import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;
import com.shopee.banking.bams.common.util.Asserter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerName {
    private String name;

    public CustomerName(String name){
        Asserter.assertNotNull(name, ParamErrorCode.NULL_PARAM);
        Asserter.assertTrue(!name.isBlank(), ParamErrorCode.INVALID_PARAM);
        Asserter.assertTrue(name.length() <= 250, ParamErrorCode.INVALID_PARAM);
        this.name = name;
    }
}
