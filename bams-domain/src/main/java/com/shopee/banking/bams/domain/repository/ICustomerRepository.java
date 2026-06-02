package com.shopee.banking.bams.domain.repository;

import com.shopee.banking.bams.domain.aggregateRoot.Customer;

import java.time.LocalDateTime;
import java.util.List;

public interface ICustomerRepository {
    Customer getCustomerByAccNo(String accNo);
    List<Customer> selectCustomersByDates(LocalDateTime startDate, LocalDateTime endDate, int numRows);
    int batchUpsert(List<Customer> customers);
}
