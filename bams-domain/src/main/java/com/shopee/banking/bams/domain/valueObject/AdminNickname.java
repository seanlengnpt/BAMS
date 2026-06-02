package com.shopee.banking.bams.domain.valueObject;

import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;
import lombok.Getter;
import lombok.Setter;
import com.shopee.banking.bams.common.util.Asserter;

@Getter
@Setter
public class AdminNickname {
    private String adminNickname;
    public AdminNickname(String adminNickname){
        Asserter.assertNotNull(adminNickname, ParamErrorCode.NULL_PARAM);
        this.adminNickname = adminNickname;
    }
}
