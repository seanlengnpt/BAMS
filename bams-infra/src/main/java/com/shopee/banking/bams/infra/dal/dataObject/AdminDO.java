package com.shopee.banking.bams.infra.dal.dataObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AdminDO extends BaseDataObject{
    private String username;
    private String password;
    private String nickname;
    private String profilePictureUrl;
}
