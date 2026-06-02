package com.shopee.banking.bams.common.util;

import com.shopee.banking.bams.common.exception.ErrorType;
import com.shopee.banking.bams.common.exception.factory.ExceptionFactory;

public class Asserter {
    public static void assertNotNull(Object param, ErrorType errorType){
        if (param == null){
            ExceptionFactory.throwException(errorType);
        }
    }

    public static void assertNotNull(Object param, ErrorType errorType, Object... args){
        if (param == null){
            ExceptionFactory.throwException(errorType, args);
        }
    }

    public static void assertTrue(boolean condition, ErrorType errorType){
        if (!condition){
            ExceptionFactory.throwException(errorType);
        }
    }

    public static void assertTrue(boolean condition, ErrorType errorType, Object... args){
        if (!condition){
            ExceptionFactory.throwException(errorType, args);
        }
    }
}
