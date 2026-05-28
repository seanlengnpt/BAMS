package com.shopee.banking.bams.domain.valueObject;

import com.shopee.banking.bams.common.enums.ParamErrorCode;
import lombok.Getter;
import com.shopee.banking.bams.common.util.Asserter;

@Getter
public class AdminId {
    private Long id;
    private AdminId(){

    }

    public AdminId(Long id){
        Asserter.assertNotNull(id, ParamErrorCode.NULL_PARAM);
        this.id = id;
    }

    public AdminId(String id){
        Long longId = Long.parseLong(id);
        Asserter.assertNotNull(longId, ParamErrorCode.NULL_PARAM);
        this.id = longId;
    }
}
