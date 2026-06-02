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


    public List<Customer> selectCustomersByDates(LocalDateTime startDate, LocalDateTime endDate) {
        Asserter.assertNotNull(startDate, ParamErrorCode.NULL_PARAM, startDate);
        Asserter.assertNotNull(endDate, ParamErrorCode.NULL_PARAM, endDate);
        try{
            List<List<Customer>> shardCustomerLists = new ArrayList<>(CUSTOMER_SHARD_COUNT);
            for (int shardIndex = 0; shardIndex < CUSTOMER_SHARD_COUNT; shardIndex++) {
                String tableName = CUSTOMER_SHARD_TABLE_PREFIX + shardIndex;
                List<CustomerDO> customerDOs = customerMapper.selectCustomersByDates(tableName, startDate, endDate);
                List<Customer> customers = customerDOs.stream()
                        .map(customerDataConverter::toEntity)
                        .toList();
                shardCustomerLists.add(customers);
            }
            return mergeSortedCustomerLists(shardCustomerLists);
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

    private List<Customer> mergeSortedCustomerLists(List<List<Customer>> shardCustomerLists) {
        List<Customer> result = new ArrayList<>();
        PriorityQueue<CustomerCursor> minHeap = new PriorityQueue<>(
                Comparator.comparing(cursor -> cursor.customer().getCreatedAt())
        );

        for (int shardIndex = 0; shardIndex < shardCustomerLists.size(); shardIndex++) {
            List<Customer> shardCustomers = shardCustomerLists.get(shardIndex);
            if (!shardCustomers.isEmpty()) {
                minHeap.offer(new CustomerCursor(shardCustomers.get(0), shardIndex, 0));
            }
        }

        while (!minHeap.isEmpty()) {
            CustomerCursor earliest = minHeap.poll();
            result.add(earliest.customer());

            int nextCustomerIndex = earliest.customerIndex() + 1;
            List<Customer> shardCustomers = shardCustomerLists.get(earliest.shardIndex());
            if (nextCustomerIndex < shardCustomers.size()) {
                minHeap.offer(new CustomerCursor(
                        shardCustomers.get(nextCustomerIndex),
                        earliest.shardIndex(),
                        nextCustomerIndex
                ));
            }
        }

        return result;
    }

    private record CustomerCursor(
            Customer customer,
            int shardIndex,
            int customerIndex
    ) {
    }

}
