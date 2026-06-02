package com.shopee.banking.bams.domain.repository;

import com.shopee.banking.bams.domain.aggregateRoot.Customer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

public interface ICustomerRepository {
    Customer getCustomerByAccNo(String accNo);
    void selectCustomersByDates(LocalDateTime startDate, LocalDateTime endDate, int numRows, Consumer<Customer> consumer);
    int batchUpsert(List<Customer> customers);
}
