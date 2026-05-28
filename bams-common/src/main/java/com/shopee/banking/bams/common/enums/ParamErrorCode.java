package com.shopee.banking.bams.common.enums;

import com.shopee.banking.bams.common.ErrorType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ParamErrorCode implements ErrorType {
    //Param error code starts with 3
    INVALID_PARAM(30001, "Input param is invalid!"),
    NULL_PARAM(30003, "Param {0} is not found!"),
    MISSING_PROPERTY(30002, "Property {0} is not found!");
    private final int code;
    private final String msg;
}
