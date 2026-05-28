package com.shopee.banking.bams.adapter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.shopee.banking.bams.infra.dal.mapper")
@SpringBootApplication(scanBasePackages = "com.shopee.banking.bams")
public class BamsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BamsApplication.class, args);
    }
}
