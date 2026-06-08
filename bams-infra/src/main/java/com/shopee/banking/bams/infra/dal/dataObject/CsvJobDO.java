package com.shopee.banking.bams.infra.dal.dataObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class CsvJobDO extends BaseDataObject {
    private String jobType;
    private Long adminId;
    private LocalDateTime createdAt;
    private String csvFilePath;
    private String status;
    private Long modifiedCount;
    private String errorMessage;
    private Integer errorCode;
}
