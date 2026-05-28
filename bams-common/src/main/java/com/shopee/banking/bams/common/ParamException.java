package com.shopee.banking.bams.common;

import com.shopee.banking.bams.common.enums.ParamErrorCode;

public class ParamException extends BaseException{
    public ParamException(ParamErrorCode errorType){
        super(errorType);
    }

    public ParamException(ParamErrorCode errorType, Object... args){
        super(errorType, args);
    }
}
