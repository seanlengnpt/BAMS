package com.shopee.banking.bams.app.service;

import com.shopee.banking.bams.app.service.dto.CreateCustomerByCsvResult;
import com.shopee.banking.bams.domain.aggregateRoot.Customer;

import java.time.LocalDateTime;

public interface ICustomerService {
    CreateCustomerByCsvResult createCustomerByCSV(String csvFilePath);
    Customer viewCustomerProfile(String accNo);
    String exportCustomersByDates(LocalDateTime startDate, LocalDateTime endDate);
}
