package com.shopee.banking.bams.infra.dal.dataObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class CustomerDO extends BaseDataObject {
    private String accountNumber;
    private String name;
    private String gender;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
