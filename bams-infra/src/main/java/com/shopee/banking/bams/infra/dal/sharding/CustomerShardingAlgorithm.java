package com.shopee.banking.bams.infra.dal.sharding;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;

public class CustomerShardingAlgorithm implements StandardShardingAlgorithm<String> {

    private static final int SHARD_COUNT = 10;
    private static final String TABLE_PREFIX = "customers_";


    @Override
    public String doSharding(Collection<String> availableTargetNames,
                             PreciseShardingValue<String> shardingValue) {
        String accountNumber = shardingValue.getValue();

        int shard = Math.floorMod(accountNumber.hashCode(), SHARD_COUNT);
        String targetTable = TABLE_PREFIX + shard;

        if (availableTargetNames.contains(targetTable)) {
            return targetTable;
        }

        throw new IllegalArgumentException("No matching shard found: " + targetTable);
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames,
                                         RangeShardingValue<String> shardingValue) {
        return availableTargetNames;
    }


    @Override
    public String getType() {
        return "CUSTOMER_SHARDING";
    }
}