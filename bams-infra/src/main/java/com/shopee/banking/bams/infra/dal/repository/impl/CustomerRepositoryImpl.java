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
import java.util.Collection;
import java.util.List;

@Repository
public class CustomerRepositoryImpl implements ICustomerRepository {
    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private CustomerDataConverter customerDataConverter;

    @Autowired
    private CustomerShardRouter customerShardRouter;

    @Override
    public List<String> findExistingAccountNumbers(Collection<String> accountNumbers) {
        Asserter.assertNotNull(accountNumbers, ParamErrorCode.NULL_PARAM, "Account numbers");
        if (accountNumbers.isEmpty()) {
            return List.of();
        }
        try {
            return customerMapper.selectExistingAccountNumbers(accountNumbers);
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            throw new DependencyException(DependencyErrorCode.DATABASE_QUERY_FAILED, accountNumbers);
        }
    }

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
    public List<Customer> selectCustomersByDates(String tableName, LocalDateTime startDate, LocalDateTime endDate) {
        Asserter.assertNotNull(startDate, ParamErrorCode.NULL_PARAM, startDate);
        Asserter.assertNotNull(endDate, ParamErrorCode.NULL_PARAM, endDate);
        try{
            List<CustomerDO> customerDOs = customerMapper.selectCustomersByDates(tableName,startDate, endDate);
            return customerDOs.stream().map(customerDO -> customerDataConverter.toEntity(customerDO)).toList();
        }catch(Throwable e){
            System.out.println(e.getMessage());
            throw new DependencyException(DependencyErrorCode.DATABASE_QUERY_FAILED, "Select customers by " + startDate + " , " + endDate);
        }
    }

    @Override
    public int batchInsert(List<Customer> customers) {
        Asserter.assertNotNull(customers, ParamErrorCode.NULL_PARAM, "Customers");
        if (customers.isEmpty()) {
            return 0;
        }
        List<CustomerDO> customerDOs = customers.stream()
                .map(customerDataConverter::toDataObject)
                .toList();
        try {
            return customerMapper.batchInsert(customerDOs);
        } catch (Throwable e) {
            throw new DependencyException(DependencyErrorCode.DATABASE_INSERT_FAILED, customers);
        }
    }

    @Override
    public int batchUpdate(List<Customer> customers) {
        Asserter.assertNotNull(customers, ParamErrorCode.NULL_PARAM, "Customers");
        if (customers.isEmpty()){
            return 0;
        }
        List<CustomerDO> customerDOs = customers.stream()
                .map(customerDataConverter::toDataObject)
                .toList();
        try {
            return customerMapper.batchUpdate(customerDOs);
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            throw new DependencyException(DependencyErrorCode.DATABASE_UPDATE_FAILED, customers);
        }
    }


}
