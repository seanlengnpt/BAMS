package com.shopee.banking.bams.common;

import com.shopee.banking.bams.common.enums.BizErrorCode;

public class BizException extends BaseException{
    public BizException(BizErrorCode errorType){
        super(errorType);
    }

    public BizException(BizErrorCode errorType, Object... args){
        super(errorType, args);
    }
}
