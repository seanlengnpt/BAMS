package com.shopee.banking.bams.common.enums;

import com.shopee.banking.bams.common.ErrorType;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum BizErrorCode implements ErrorType {
    ADMIN_NOT_FOUND_MAPPING(40001, "Admin not found with id = {0}");

    private final int code;
    private final String msg;
}
