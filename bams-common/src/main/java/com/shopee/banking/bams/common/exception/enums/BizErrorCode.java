package com.shopee.banking.bams.common.exception.enums;

import com.shopee.banking.bams.common.exception.ErrorType;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum BizErrorCode implements ErrorType {
    ADMIN_NOT_FOUND_MAPPING(40001, "Admin not found with id = {0}"),
    CUSTOMER_NOT_FOUND_MAPPING(40002, "Customer not found with accNo = {0}"),
    CUSTOMERS_NOT_FOUND_EXPORT(40003, "Customers not found with details matching: {0}"),

    INVALID_CSV_FILEPATH(40004, "CSV file path is invalid"),
    INVALID_CSV_FILE(40005, "Invalid csv file! Errors: {0}"),
    STALE_UPDATE(40006, "There have been changes to {0} since the request was made. Please try again.");

    private final int code;
    private final String msg;
}
