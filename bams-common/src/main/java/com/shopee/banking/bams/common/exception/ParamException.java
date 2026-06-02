package com.shopee.banking.bams.common.exception;

import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;

public class ParamException extends BaseException {
    public ParamException(ParamErrorCode errorType){
        super(errorType);
    }

    public ParamException(ParamErrorCode errorType, Object... args){
        super(errorType, args);
    }
}
