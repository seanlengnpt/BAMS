package com.shopee.banking.bams.common.enums;

import com.shopee.banking.bams.common.ErrorType;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum BizErrorCode implements ErrorType {
    ADMIN_NOT_FOUND_MAPPING(40001, "Admin not found with id = {0}"),
    CUSTOMER_NOT_FOUND_MAPPING(40002, "Customer not found with accNo = {0}"),
    INVALID_CSV_FILEPATH(40003, "CSV file path is invalid"),
    INVALID_CSV_FILE(40004, "Invalid csv file! Errors: {0}");

    private final int code;
    private final String msg;
}
