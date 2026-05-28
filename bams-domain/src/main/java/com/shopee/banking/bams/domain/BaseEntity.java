package com.shopee.banking.bams.domain;

public class BaseEntity {
    private Long version;

    public BaseEntity(){

    }

    public BaseEntity(Long version){
        this.version = version;
    }

    public void incrementVersion(){
        if (this.version == null){
            this.version = 0L;
        }else{
            this.version++;
        }
    }
}
