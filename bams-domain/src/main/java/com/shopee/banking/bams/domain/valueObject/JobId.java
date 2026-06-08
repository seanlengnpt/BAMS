package com.shopee.banking.bams.domain.valueObject;

import com.shopee.banking.bams.common.exception.ParamException;
import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;
import com.shopee.banking.bams.common.util.Asserter;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class JobId {
    private Long id;

    private JobId() {
    }

    public JobId(Long id) {
        Asserter.assertNotNull(id, ParamErrorCode.NULL_PARAM);
        this.id = id;
    }

    public JobId(String id) {
        try {
            Long longId = Long.parseLong(id);
            Asserter.assertNotNull(longId, ParamErrorCode.NULL_PARAM);
            this.id = longId;
        } catch (NumberFormatException e) {
            throw new ParamException(ParamErrorCode.INVALID_PARAM, "jobId");
        }
    }
}
