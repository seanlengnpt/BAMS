package com.shopee.banking.bams.domain.aggregateRoot;


import com.shopee.banking.bams.domain.BaseEntity;
import lombok.Getter;

@Getter
public class BaseAggregateRoot extends BaseEntity {
    private Long id;
    protected BaseAggregateRoot(){

    }
    public BaseAggregateRoot(Long id){
        this.id = id;
    }
}
