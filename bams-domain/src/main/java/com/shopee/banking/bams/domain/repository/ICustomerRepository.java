package com.shopee.banking.bams.domain.repository;

import com.shopee.banking.bams.domain.aggregateRoot.Customer;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ICustomerRepository {
    List<String> findExistingAccountNumbers(Collection<String> accountNumbers);
    Customer getCustomerByAccNo(String accNo);
    List<Customer> selectCustomersByDates(String tableName, LocalDateTime startDate, LocalDateTime endDate);
    int batchInsert(List<Customer> customers);
    int batchUpdate(List<Customer> customers);
}
