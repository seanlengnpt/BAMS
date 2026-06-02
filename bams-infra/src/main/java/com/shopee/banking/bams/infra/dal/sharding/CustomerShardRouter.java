package com.shopee.banking.bams.infra.dal.sharding;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class CustomerShardRouter {

    private static final int SHARD_COUNT = 10;
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("\\d+");

    public String getTableName(String accountNumber) {
        if (accountNumber == null || !ACCOUNT_NUMBER_PATTERN.matcher(accountNumber).matches()) {
            throw new IllegalArgumentException("Invalid account number");
        }

        int shard = Math.floorMod(accountNumber.hashCode(), SHARD_COUNT);
        return "customers_" + shard;
    }
}