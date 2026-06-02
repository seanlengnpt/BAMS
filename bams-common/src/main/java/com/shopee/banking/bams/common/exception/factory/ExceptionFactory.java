package com.shopee.banking.bams.common.exception.factory;

import com.shopee.banking.bams.common.exception.*;
import com.shopee.banking.bams.common.exception.enums.AuthErrorCode;
import com.shopee.banking.bams.common.exception.enums.BizErrorCode;
import com.shopee.banking.bams.common.exception.enums.DependencyErrorCode;
import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;
import com.shopee.banking.bams.common.exception.enums.SystemErrorCode;

public class ExceptionFactory {
    public static void throwException(ErrorType errorType){
        switch (errorType) {
            case ParamErrorCode paramErrorCode -> throw new ParamException(paramErrorCode);
            case BizErrorCode bizErrorCode -> throw new BizException(bizErrorCode);
            case SystemErrorCode systemErrorCode -> throw new SystemException(systemErrorCode);
            case AuthErrorCode authErrorCode -> throw new AuthException(authErrorCode);
            case DependencyErrorCode dependencyErrorCode -> throw new DependencyException(dependencyErrorCode);
            default -> throw new BaseException(errorType);
        }
    }

    public static void throwException(ErrorType errorType, Object... args){
        switch (errorType) {
            case ParamErrorCode paramErrorCode -> throw new ParamException(paramErrorCode, args);
            case BizErrorCode bizErrorCode -> throw new BizException(bizErrorCode, args);
            case SystemErrorCode systemErrorCode -> throw new SystemException(systemErrorCode, args);
            case AuthErrorCode authErrorCode -> throw new AuthException(authErrorCode, args);
            case DependencyErrorCode dependencyErrorCode -> throw new DependencyException(dependencyErrorCode, args);
            default -> throw new BaseException(errorType);
        }
    }


}
