package com.shopee.banking.bams.infra.dal.repository.impl;

import com.shopee.banking.bams.common.DependencyException;
import com.shopee.banking.bams.common.enums.DependencyErrorCode;
import com.shopee.banking.bams.common.enums.ParamErrorCode;
import com.shopee.banking.bams.common.util.Asserter;
import com.shopee.banking.bams.domain.aggregateRoot.Customer;
import com.shopee.banking.bams.domain.repository.ICustomerRepository;
import com.shopee.banking.bams.infra.dal.converter.CustomerDataConverter;
import com.shopee.banking.bams.infra.dal.dataObject.CustomerDO;
import com.shopee.banking.bams.infra.dal.mapper.CustomerMapper;
import com.shopee.banking.bams.infra.dal.sharding.CustomerShardRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

@Repository
public class CustomerRepositoryImpl implements ICustomerRepository {
    private static final int CUSTOMER_SHARD_COUNT = 10;
    private static final String CUSTOMER_SHARD_TABLE_PREFIX = "customers_";
    private static final int SHARD_FETCH_SIZE = 1000;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private CustomerDataConverter customerDataConverter;

    @Autowired
    private CustomerShardRouter customerShardRouter;


    @Override
    public Customer getCustomerByAccNo(String accNo) {
        Asserter.assertNotNull(accNo, ParamErrorCode.NULL_PARAM, "accNo");
        try{
            CustomerDO customerDO = customerMapper.selectCustomerByAccNo(accNo, customerShardRouter.getTableName(accNo));
            return customerDataConverter.toEntity(customerDO);
        }catch (Throwable e) {
            throw new DependencyException(DependencyErrorCode.DATABASE_QUERY_FAILED, accNo);
        }
    }

    @Override
    public List<Customer> selectCustomersByDates(LocalDateTime startDate, LocalDateTime endDate, int numRows) {
        Asserter.assertNotNull(startDate, ParamErrorCode.NULL_PARAM, "startDate");
        Asserter.assertNotNull(endDate, ParamErrorCode.NULL_PARAM, "endDate");
        Asserter.assertTrue(!startDate.isAfter(endDate), ParamErrorCode.INVALID_PARAM, "startDate, endDate");
        Asserter.assertTrue(numRows > 0, ParamErrorCode.INVALID_PARAM, "numRows");
        try{
            List<List<Customer>> shardCustomerLists = new ArrayList<>(CUSTOMER_SHARD_COUNT);
            for (int shardIndex = 0; shardIndex < CUSTOMER_SHARD_COUNT; shardIndex++) {
                shardCustomerLists.add(refillCustomersInShard(
                        getShardTableName(shardIndex),
                        startDate,
                        endDate,
                        0
                ));
            }
            return mergeSortedCustomerLists(shardCustomerLists, startDate, endDate, numRows);
        }catch(Throwable e){
            throw new DependencyException(DependencyErrorCode.DATABASE_QUERY_FAILED, "Select customers by " + startDate + " , " + endDate);
        }
    }


    @Override
    public int batchUpsert(List<Customer> customers) {
        Asserter.assertNotNull(customers, ParamErrorCode.NULL_PARAM, "Customers");
        if (customers.isEmpty()) {
            return 0;
        }

        Map<String, List<CustomerDO>> customersByTable = customers.stream()
                .map(customerDataConverter::toDataObject)
                .collect(Collectors.groupingBy(
                        customerDO -> customerShardRouter.getTableName(customerDO.getAccountNumber()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        try {
            int modifiedCount = 0;
            for (Map.Entry<String, List<CustomerDO>> entry : customersByTable.entrySet()) {
                modifiedCount += customerMapper.batchUpsert(entry.getKey(), entry.getValue());
            }
            return modifiedCount;
        } catch (Throwable e) {
            throw new DependencyException(DependencyErrorCode.DATABASE_UPDATE_FAILED, customers);
        }
    }

    private List<Customer> refillCustomersInShard(String tableName,
                                                  LocalDateTime startDate,
                                                  LocalDateTime endDate,
                                                  int offset) {
        List<CustomerDO> customerDOs = customerMapper.selectCustomersByDates(tableName, startDate, endDate, offset);
        List<Customer> customers = new ArrayList<>(customerDOs.size());
        for (CustomerDO customerDO : customerDOs) {
            customers.add(customerDataConverter.toEntity(customerDO));
        }
        return customers;
    }

    private List<Customer> mergeSortedCustomerLists(List<List<Customer>> shardCustomerLists,
                                                    LocalDateTime startDate,
                                                    LocalDateTime endDate,
                                                    int maxCustomers) {
        List<Customer> result = new ArrayList<>(Math.min(maxCustomers, SHARD_FETCH_SIZE));
        PriorityQueue<CustomerCursor> minHeap = new PriorityQueue<>(
                Comparator.comparing(cursor -> cursor.customer().getCreatedAt())
        );
        int[] shardOffsets = new int[shardCustomerLists.size()];

        for (int shardIndex = 0; shardIndex < shardCustomerLists.size(); shardIndex++) {
            List<Customer> shardCustomers = shardCustomerLists.get(shardIndex);
            shardOffsets[shardIndex] = shardCustomers.size();
            if (!shardCustomers.isEmpty()) {
                minHeap.offer(new CustomerCursor(shardCustomers.get(0), shardIndex, 0));
            }
        }

        while (!minHeap.isEmpty() && result.size() < maxCustomers) {
            CustomerCursor earliest = minHeap.poll();
            result.add(earliest.customer());
            if (result.size() >= maxCustomers) {
                break;
            }

            int nextCustomerIndex = earliest.customerIndex() + 1;
            List<Customer> shardCustomers = shardCustomerLists.get(earliest.shardIndex());
            if (nextCustomerIndex < shardCustomers.size()) {
                minHeap.offer(new CustomerCursor(
                        shardCustomers.get(nextCustomerIndex),
                        earliest.shardIndex(),
                        nextCustomerIndex
                ));
            } else {
                int shardIndex = earliest.shardIndex();
                List<Customer> refilledCustomers = refillCustomersInShard(
                        getShardTableName(shardIndex),
                        startDate,
                        endDate,
                        shardOffsets[shardIndex]
                );
                shardCustomerLists.set(shardIndex, refilledCustomers);
                shardOffsets[shardIndex] += refilledCustomers.size();
                if (!refilledCustomers.isEmpty()) {
                    minHeap.offer(new CustomerCursor(refilledCustomers.get(0), shardIndex, 0));
                }
            }
        }

        return result;
    }

    private String getShardTableName(int shardIndex) {
        return CUSTOMER_SHARD_TABLE_PREFIX + shardIndex;
    }

    private record CustomerCursor(
            Customer customer,
            int shardIndex,
            int customerIndex
    ) {
    }

}
