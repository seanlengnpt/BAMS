package com.shopee.banking.bams.infra.dal.dataObject;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseDataObject {

    private Long id;

    private Long createTimestamp;

    private Long updateTimestamp;

    private String createUserId = "promotion-service";

    private String updateUserId = "promotion-service";

    private Long version;

    public BaseDataObject() {
    }

    public BaseDataObject(Long id) {
        this.id = id;
    }
}
