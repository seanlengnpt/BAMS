package com.shopee.banking.bams.infra.dal.repository.impl;

import com.shopee.banking.bams.common.exception.BaseException;
import com.shopee.banking.bams.common.exception.DependencyException;
import com.shopee.banking.bams.common.enums.CsvJobStatus;
import com.shopee.banking.bams.common.exception.enums.BizErrorCode;
import com.shopee.banking.bams.common.exception.enums.DependencyErrorCode;
import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;
import com.shopee.banking.bams.common.util.Asserter;
import com.shopee.banking.bams.domain.aggregateRoot.Customer;
import com.shopee.banking.bams.domain.repository.ICustomerRepository;
import com.shopee.banking.bams.domain.valueObject.AdminId;
import com.shopee.banking.bams.domain.valueObject.JobId;
import com.shopee.banking.bams.infra.dal.converter.CustomerDataConverter;
import com.shopee.banking.bams.infra.dal.dataObject.CsvJobDO;
import com.shopee.banking.bams.infra.dal.dataObject.CustomerDO;
import com.shopee.banking.bams.infra.dal.mapper.CsvJobMapper;
import com.shopee.banking.bams.infra.dal.mapper.CustomerMapper;
import com.shopee.banking.bams.infra.dal.sharding.CustomerShardRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Repository
public class CustomerRepositoryImpl implements ICustomerRepository {
    private static final int CUSTOMER_SHARD_COUNT = 10;
    private static final String CUSTOMER_SHARD_TABLE_PREFIX = "customers_";
    private static final int SHARD_FETCH_SIZE = 1000;
    private static final String JOB_TYPE_IMPORT = "import";
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("\\d{10}");

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private CsvJobMapper csvJobMapper;

    @Autowired
    private CustomerDataConverter customerDataConverter;

    @Autowired
    private CustomerShardRouter customerShardRouter;

    @Override
    public JobId createJob(AdminId adminId, String csvFilePath) {
        Asserter.assertNotNull(adminId, ParamErrorCode.NULL_PARAM, "adminId");
        Asserter.assertNotNull(csvFilePath, ParamErrorCode.NULL_PARAM, "csvFilePath");
        Asserter.assertTrue(!csvFilePath.isBlank(), ParamErrorCode.INVALID_PARAM, "csvFilePath");
        CsvJobDO jobDO = new CsvJobDO();
        jobDO.setJobType(JOB_TYPE_IMPORT);
        jobDO.setAdminId(adminId.getId());
        jobDO.setCsvFilePath(csvFilePath);
        jobDO.setStatus(CsvJobStatus.PENDING.name());
        jobDO.setModifiedCount(0L);
        try {
            csvJobMapper.insertJob(jobDO);
            return new JobId(jobDO.getId());
        } catch (Throwable e) {
            throw new DependencyException(DependencyErrorCode.DATABASE_INSERT_FAILED, "csv job");
        }
    }

    @Override
    public void markCsvJobSuccess(JobId jobId, int modifiedCount) {
        Asserter.assertNotNull(jobId, ParamErrorCode.NULL_PARAM, "jobId");
        try {
            csvJobMapper.updateJobSuccess(jobId.getId(), modifiedCount, CsvJobStatus.SUCCESS.name());
        } catch (Throwable e) {
            throw new DependencyException(DependencyErrorCode.DATABASE_UPDATE_FAILED, "csv job success");
        }
    }

    @Override
    public void markCsvJobFail(JobId jobId, String operation, int errorCode, String errorMessage) {
        Asserter.assertNotNull(jobId, ParamErrorCode.NULL_PARAM, "jobId");
        Asserter.assertNotNull(operation, ParamErrorCode.NULL_PARAM, "operation");
        try {
            csvJobMapper.updateJobFail(
                    jobId.getId(),
                    CsvJobStatus.FAIL.name(),
                    errorCode,
                    errorMessage
            );
        } catch (Throwable e) {
            throw new DependencyException(DependencyErrorCode.DATABASE_UPDATE_FAILED, operation + " csv job fail");
        }
    }


    @Override
    public Customer getCustomerByAccNo(String accNo) {
        Asserter.assertNotNull(accNo, ParamErrorCode.NULL_PARAM, "accNo");
        Asserter.assertTrue(ACCOUNT_NUMBER_PATTERN.matcher(accNo).matches(), ParamErrorCode.INVALID_PARAM, "accNo");
        try{
            CustomerDO customerDO = customerMapper.selectCustomerByAccNo(accNo, customerShardRouter.getTableName(accNo));
            return customerDataConverter.toEntity(customerDO);
        } catch (BaseException e) {
            throw e;
        }catch (Throwable e) {
            throw new DependencyException(DependencyErrorCode.DATABASE_QUERY_FAILED, accNo);
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

    @Override
    public void selectCustomersByDates(LocalDateTime startDate, LocalDateTime endDate, int numRows, Consumer<Customer> consumer) {
        Asserter.assertNotNull(startDate, ParamErrorCode.NULL_PARAM, "startDate");
        Asserter.assertNotNull(endDate, ParamErrorCode.NULL_PARAM, "endDate");
        Asserter.assertTrue(!startDate.isAfter(endDate), ParamErrorCode.INVALID_PARAM, "startDate, endDate");
        Asserter.assertTrue(numRows > 0, ParamErrorCode.INVALID_PARAM, "numRows");
        Asserter.assertNotNull(consumer, ParamErrorCode.NULL_PARAM, "consumer");
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
            Asserter.assertTrue(
                    shardCustomerLists.stream().anyMatch(shardCustomers -> !shardCustomers.isEmpty()),
                    BizErrorCode.CUSTOMERS_NOT_FOUND_EXPORT,
                    "createdAt between " + startDate + " and " + endDate
            );
            mergeSortedCustomerLists(shardCustomerLists, startDate, endDate, numRows, consumer);
        } catch (UncheckedIOException | BaseException e) {
            throw e;
        } catch(Throwable e){
            throw new DependencyException(DependencyErrorCode.DATABASE_QUERY_FAILED, "Select customers by " + startDate + " , " + endDate);
        }
    }


    private List<Customer> refillCustomersInShard(String tableName,
                                                  LocalDateTime startDate,
                                                  LocalDateTime endDate,
                                                  int offset) {
        List<CustomerDO> customerDOs = customerMapper.selectCustomersByDates(tableName, startDate, endDate, SHARD_FETCH_SIZE, offset);
        List<Customer> customers = new ArrayList<>(customerDOs.size());
        for (CustomerDO customerDO : customerDOs) {
            customers.add(customerDataConverter.toEntity(customerDO));
        }
        return customers;
    }

    private void mergeSortedCustomerLists(List<List<Customer>> shardCustomerLists,
                                          LocalDateTime startDate,
                                          LocalDateTime endDate,
                                          int maxCustomers,
                                          Consumer<Customer> consumer) {
        PriorityQueue<CustomerCursor> minHeap = new PriorityQueue<>(
                Comparator.comparing(cursor -> cursor.customer().getCreatedAt())
        );
        int[] shardOffsets = new int[shardCustomerLists.size()];
        int rowCount = 0;

        for (int shardIndex = 0; shardIndex < shardCustomerLists.size(); shardIndex++) {
            List<Customer> shardCustomers = shardCustomerLists.get(shardIndex);
            shardOffsets[shardIndex] = shardCustomers.size();
            if (!shardCustomers.isEmpty()) {
                minHeap.offer(new CustomerCursor(shardCustomers.get(0), shardIndex, 0));
            }
        }

        while (!minHeap.isEmpty() && rowCount < maxCustomers) {
            CustomerCursor earliest = minHeap.poll();
            consumer.accept(earliest.customer());
            rowCount++;

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
