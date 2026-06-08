package com.shopee.banking.bams.app.service;

import com.shopee.banking.bams.app.service.dto.CreateCustomerByCsvResult;
import com.shopee.banking.bams.domain.aggregateRoot.Customer;
import com.shopee.banking.bams.domain.valueObject.JobId;

import java.nio.file.Path;
import java.time.LocalDateTime;

public interface ICustomerService {
    CreateCustomerByCsvResult createCustomerByCsvJob(String csvFilePath, String adminId);
    void processCsvJob(JobId jobId, Path csvPath);
    Customer viewCustomerProfile(String accNo);
    String exportCustomersByDates(LocalDateTime startDate, LocalDateTime endDate);
}
