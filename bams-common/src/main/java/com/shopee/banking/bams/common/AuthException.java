package com.shopee.banking.bams.common;

import com.shopee.banking.bams.common.enums.AuthErrorCode;

public class AuthException extends BaseException{
    public AuthException(AuthErrorCode errorType){
        super(errorType);
    }

    public AuthException(AuthErrorCode errorType, Object... args){
        super(errorType, args);
    }
}
