package com.shopee.banking.bams.domain.valueObject;

import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;
import lombok.Getter;
import lombok.Setter;
import com.shopee.banking.bams.common.util.Asserter;

@Getter
@Setter
public class AdminProfilePictureUrl {
    private String adminProfilePictureUrl;
    public AdminProfilePictureUrl(String adminProfilePictureUrl){
        Asserter.assertNotNull(adminProfilePictureUrl, ParamErrorCode.NULL_PARAM);
        this.adminProfilePictureUrl = adminProfilePictureUrl;
    }
}
