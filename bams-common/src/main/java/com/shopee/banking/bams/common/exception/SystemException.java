package com.shopee.banking.bams.common.exception;
import com.shopee.banking.bams.common.exception.enums.SystemErrorCode;

public class SystemException extends BaseException {
    public SystemException(SystemErrorCode errorType) {
        super(errorType);
    }

    public SystemException(SystemErrorCode errorType, Object... args){
        super(errorType, args);
    }
}
