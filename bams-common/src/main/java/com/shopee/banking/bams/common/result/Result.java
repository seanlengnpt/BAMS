package com.shopee.banking.bams.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shopee.banking.bams.common.BaseException;
import com.shopee.banking.bams.common.ErrorType;
import lombok.Data;

@Data
public class Result<T> {
    public static final int SUCCESS_CODE = 0;
    public static final String SUCCESS_MSG = "SUCCESS";

    private int code;

    private String msg;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    //Constructors

    private Result(ErrorType errorType){
        this.code = errorType.getCode();
        this.msg = errorType.getMsg();
    }

    private Result(ErrorType errorType, T data){
        this(errorType);
        this.data = data;
    }

    private Result(int code, String msg, T data){
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success(T data){
        return new Result<>(SUCCESS_CODE, SUCCESS_MSG, data);
    }

    public static <T> Result<T> success() {return success(null);}

    public static <T> Result<T> fail(BaseException e){return new Result<>(e.getErrorType().getCode(), e.getMessage(), null);}

    public static <T> Result<T> fail(ErrorType e){return new Result<>(e);}

}
