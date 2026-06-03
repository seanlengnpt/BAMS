package com.shopee.banking.bams.infra.dal.dataObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class AdminDO extends BaseDataObject{
    private String username;
    private String password;
    private String nickname;
    private String profilePictureUrl;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
