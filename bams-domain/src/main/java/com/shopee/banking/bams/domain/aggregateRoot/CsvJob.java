package com.shopee.banking.bams.domain.aggregateRoot;

import com.shopee.banking.bams.common.enums.CsvJobStatus;
import com.shopee.banking.bams.domain.valueObject.AdminId;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CsvJob extends BaseAggregateRoot {
    private Long id;
    private AdminId adminId;
    private LocalDateTime createdAt;
    private CsvJobStatus status;
    private Long modifiedCount;
    private String errorMessage;
    private Integer errorCode;
}
