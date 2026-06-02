package com.shopee.banking.bams.common.exception.enums;

import com.shopee.banking.bams.common.exception.ErrorType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DependencyErrorCode implements ErrorType {
    //Dependency error code starts with 2
    DATABASE_INSERT_FAILED(20001, "Database insert failed for {0}."),
    DATABASE_UPDATE_FAILED(20002, "Database update failed for {0}."),
    DATABASE_QUERY_FAILED(20003, "Database query failed for {0}."),

    EXPORT_CSV_FAILED(20004, "CSV export failed for {0}");

    private final int code;
    private final String msg;
}
