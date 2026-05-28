package com.shopee.banking.bams.infra.dal.converter;


import com.shopee.banking.bams.domain.BaseEntity;
import com.shopee.banking.bams.infra.dal.dataObject.BaseDataObject;

public interface BaseDataConverter<T1 extends BaseEntity, T2 extends BaseDataObject> {

    T1 toEntity(T2 t2);

    T2 toDataObject(T1 t1);
}

