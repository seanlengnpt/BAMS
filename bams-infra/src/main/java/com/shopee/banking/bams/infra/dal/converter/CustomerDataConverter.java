package com.shopee.banking.bams.infra.dal.converter;

import com.shopee.banking.bams.common.enums.Gender;
import com.shopee.banking.bams.domain.aggregateRoot.Customer;
import com.shopee.banking.bams.infra.dal.dataObject.CustomerDO;
import org.springframework.stereotype.Component;

@Component
public class CustomerDataConverter implements BaseDataConverter<Customer, CustomerDO> {
    @Override
    public Customer toEntity(CustomerDO customerDO) {
        if (customerDO == null) {
            return null;
        }
        return Customer.builder()
                .id(customerDO.getId())
                .accountNumber(customerDO.getAccountNumber())
                .gender(Gender.valueOf(customerDO.getGender()))
                .name(customerDO.getName())
                .createdAt(customerDO.getCreatedAt())
                .modifiedAt(customerDO.getModifiedAt())
                .build();
    }

    @Override
    public CustomerDO toDataObject(Customer customer) {
        if (customer == null) {
            return null;
        }
        CustomerDO customerDO = new CustomerDO();
        customerDO.setId(customer.getId());
        customerDO.setAccountNumber(customer.getAccountNumber());
        customerDO.setName(customer.getName());
        customerDO.setGender(customer.getGender().name());
        customerDO.setCreatedAt(customer.getCreatedAt());
        customerDO.setModifiedAt(customer.getModifiedAt());
        return customerDO;
    }
}
