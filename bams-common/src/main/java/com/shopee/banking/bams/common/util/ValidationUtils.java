package com.shopee.banking.bams.common.util;

import com.shopee.banking.bams.common.exception.ParamException;
import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

public class ValidationUtils{
    public static final Validator VALIDATOR;

    static{
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        VALIDATOR = factory.getValidator();
    }

    public static <R> void validate(R object) throws ParamException {
        Set<ConstraintViolation<R>> violations = VALIDATOR.validate(object);
        if (!violations.isEmpty()){
            ConstraintViolation<R> item = violations.iterator().next();
            String errMsg = item.getLeafBean().getClass().getSimpleName()
                    + " Field: " + item.getPropertyPath().toString()
                    + " Msg: " + item.getMessage();
            throw new ParamException(ParamErrorCode.INVALID_PARAM, errMsg);
        }
    }
}