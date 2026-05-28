package com.shopee.banking.bams.common;
import com.shopee.banking.bams.common.enums.ParamErrorCode;
import com.shopee.banking.bams.common.enums.SystemErrorCode;

public class SystemException extends BaseException{
    public SystemException(SystemErrorCode errorType) {
        super(errorType);
    }

    public SystemException(SystemErrorCode errorType, Object... args){
        super(errorType, args);
    }
}
