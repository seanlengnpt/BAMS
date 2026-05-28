package com.shopee.banking.bams.common;

import com.shopee.banking.bams.common.enums.DependencyErrorCode;

public class DependencyException extends BaseException{
    public DependencyException(DependencyErrorCode errorType){
        super(errorType);
    }

    public DependencyException(DependencyErrorCode errorType, Object... args){
        super(errorType, args);
    }
}
