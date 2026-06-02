package com.shopee.banking.bams.domain.valueObject;

import com.shopee.banking.bams.common.enums.ParamErrorCode;
import com.shopee.banking.bams.common.util.Asserter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerAccountNumber {
    private String accountNumber;

    public CustomerAccountNumber(String accountNumber){
        Asserter.assertNotNull(accountNumber, ParamErrorCode.NULL_PARAM);
        Asserter.assertTrue(accountNumber.length() == 10, ParamErrorCode.INVALID_PARAM);
        Asserter.assertTrue(accountNumber.chars().allMatch(Character::isDigit), ParamErrorCode.INVALID_PARAM);
        this.accountNumber = accountNumber;
    }
}
