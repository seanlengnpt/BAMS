package com.shopee.banking.bams.common.exception.enums;

import com.shopee.banking.bams.common.exception.ErrorType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SystemErrorCode implements ErrorType{
    //System error code starts with 1
    UNKNOWN_EXCEPTION(100001, "Unknown system error.");
    private final int code;
    private final String msg;
}
