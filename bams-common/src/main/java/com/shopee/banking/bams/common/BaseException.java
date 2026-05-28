package com.shopee.banking.bams.common;

import lombok.Getter;

import java.text.MessageFormat;

@Getter
public class BaseException extends RuntimeException{
    private final ErrorType errorType;

    public BaseException(ErrorType errorType){
        super(errorType.getMsg());
        this.errorType = errorType;
    }

    public BaseException(ErrorType errorType, Object... args){
        super(formatMsg(errorType.getMsg(), args));
        this.errorType = errorType;
    }

    private static String formatMsg(String pattern, Object... args) {
        return MessageFormat.format(pattern.replace("'", "''"), args);
    }
}
