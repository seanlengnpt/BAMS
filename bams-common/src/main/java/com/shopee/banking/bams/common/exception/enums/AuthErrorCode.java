package com.shopee.banking.bams.common.exception.enums;

import com.shopee.banking.bams.common.exception.ErrorType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorType {
    INVALID_CREDENTIALS(40001, "Invalid username or password."),
    INVALID_REFRESH_TOKEN(40002, "Invalid refresh token."),
    INVALID_ACCESS_TOKEN(40003, "Invalid access token.");

    private final int code;
    private final String msg;
}
