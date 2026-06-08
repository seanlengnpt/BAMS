package com.shopee.banking.bams.domain.repository;

import com.shopee.banking.bams.domain.aggregateRoot.Customer;
import com.shopee.banking.bams.domain.valueObject.AdminId;
import com.shopee.banking.bams.domain.valueObject.JobId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

public interface ICustomerRepository {
    JobId createJob(AdminId adminId, String csvFilePath);
    void markCsvJobSuccess(JobId jobId, int modifiedCount);
    void markCsvJobFail(JobId jobId, String operation, int errorCode, String errorMessage);
    Customer getCustomerByAccNo(String accNo);
    void selectCustomersByDates(LocalDateTime startDate, LocalDateTime endDate, int numRows, Consumer<Customer> consumer);
    int batchUpsert(List<Customer> customers);
}
