package com.shopee.banking.bams.infra.dal.dataObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class AdminTokenDO extends BaseDataObject {
    private Long adminId;
    private String refreshToken;
    private Boolean revoked;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
