package com.shopee.banking.bams.common.result;

import com.shopee.banking.bams.common.exception.BizException;
import com.shopee.banking.bams.common.exception.enums.BizErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResultTest {
    @Test
    void failWithBaseExceptionUsesFormattedMessage() {
        Result<Void> result = Result.fail(new BizException(BizErrorCode.ADMIN_NOT_FOUND_MAPPING, 2));

        assertEquals(40001, result.getCode());
        assertEquals("Admin not found with id = 2", result.getMsg());
    }
}
