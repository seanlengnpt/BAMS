package com.shopee.banking.bams.infra.dal.repository.impl;

import com.shopee.banking.bams.common.exception.BizException;
import com.shopee.banking.bams.common.exception.DependencyException;
import com.shopee.banking.bams.common.exception.ParamException;
import com.shopee.banking.bams.common.exception.enums.BizErrorCode;
import com.shopee.banking.bams.common.exception.enums.DependencyErrorCode;
import com.shopee.banking.bams.common.enums.Gender;
import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;
import com.shopee.banking.bams.domain.aggregateRoot.Customer;
import com.shopee.banking.bams.infra.dal.converter.CustomerDataConverter;
import com.shopee.banking.bams.infra.dal.dataObject.CustomerDO;
import com.shopee.banking.bams.infra.dal.mapper.CustomerMapper;
import com.shopee.banking.bams.infra.dal.sharding.CustomerShardRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerRepositoryImplTest {

    private static final LocalDateTime START_DATE = LocalDateTime.of(2020, 5, 1, 0, 0);
    private static final LocalDateTime END_DATE = LocalDateTime.of(2020, 5, 31, 23, 59, 59);
    private static final CustomerShardRouter CUSTOMER_SHARD_ROUTER = new CustomerShardRouter();

    private CustomerRepositoryImpl customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @BeforeEach
    void setUp() {
        customerRepository = new CustomerRepositoryImpl();
        ReflectionTestUtils.setField(customerRepository, "customerMapper", customerMapper);
        ReflectionTestUtils.setField(customerRepository, "customerDataConverter", new CustomerDataConverter());
        ReflectionTestUtils.setField(customerRepository, "customerShardRouter", CUSTOMER_SHARD_ROUTER);
    }

    @Test
    @DisplayName("getCustomerByAccNo returns customer details for valid accNo")
    void getCustomerByAccNo_validAccNo_returnsCustomer() {
        String accNo = "1000000001";
        CustomerDO customerDO = buildCustomerDO(accNo, START_DATE);
        customerDO.setId(1L);
        when(customerMapper.selectCustomerByAccNo(accNo, CUSTOMER_SHARD_ROUTER.getTableName(accNo)))
                .thenReturn(customerDO);

        Customer customer = customerRepository.getCustomerByAccNo(accNo);

        assertEquals(1L, customer.getId());
        assertEquals(accNo, customer.getAccountNumber());
        assertEquals("Name " + accNo, customer.getName());
        assertEquals(Gender.M, customer.getGender());
        assertEquals(START_DATE, customer.getCreatedAt());
        assertEquals(START_DATE.plusHours(1), customer.getModifiedAt());
        verify(customerMapper).selectCustomerByAccNo(accNo, CUSTOMER_SHARD_ROUTER.getTableName(accNo));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "123456789", "12345678901", "12345abcde"})
    @DisplayName("getCustomerByAccNo fails when accNo is invalid")
    void getCustomerByAccNo_invalidAccNo_fails(String accNo) {
        ParamException exception = assertThrows(
                ParamException.class,
                () -> customerRepository.getCustomerByAccNo(accNo)
        );

        if (accNo == null) {
            assertEquals(ParamErrorCode.NULL_PARAM, exception.getErrorType());
        } else {
            assertEquals(ParamErrorCode.INVALID_PARAM, exception.getErrorType());
        }
        verifyNoInteractions(customerMapper);
    }

    @Test
    @DisplayName("getCustomerByAccNo returns null when customer profile is not found")
    void getCustomerByAccNo_customerNotFound_returnsNull() {
        String accNo = "1000000001";
        when(customerMapper.selectCustomerByAccNo(accNo, CUSTOMER_SHARD_ROUTER.getTableName(accNo)))
                .thenReturn(null);

        Customer customer = customerRepository.getCustomerByAccNo(accNo);

        assertNull(customer);
        verify(customerMapper).selectCustomerByAccNo(accNo, CUSTOMER_SHARD_ROUTER.getTableName(accNo));
    }

    @Test
    @DisplayName("batchUpsert fails when customers is null")
    void batchUpsert_nullCustomers_fails() {
        ParamException exception = assertThrows(
                ParamException.class,
                () -> customerRepository.batchUpsert(null)
        );

        assertEquals(ParamErrorCode.NULL_PARAM, exception.getErrorType());
        verifyNoInteractions(customerMapper);
    }

    @Test
    @DisplayName("batchUpsert returns zero and skips mapper when customers is empty")
    void batchUpsert_emptyCustomers_returnsZero() {
        int modifiedCount = customerRepository.batchUpsert(List.of());

        assertEquals(0, modifiedCount);
        verifyNoInteractions(customerMapper);
    }

    @Test
    @DisplayName("batchUpsert groups customers by shard table and sums modified count")
    void batchUpsert_validCustomers_groupsByShardAndReturnsModifiedCount() {
        List<Customer> customers = List.of(
                buildCustomer("1000000001", "Alice Tan", Gender.F),
                buildCustomer("1000000002", "Bob Lim", Gender.M),
                buildCustomer("1000000003", "Casey Ng", Gender.OTHERS)
        );
        Map<String, List<Customer>> customersByTable = customers.stream()
                .collect(Collectors.groupingBy(
                        customer -> CUSTOMER_SHARD_ROUTER.getTableName(customer.getAccountNumber()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        when(customerMapper.batchUpsert(anyString(), anyList()))
                .thenAnswer(invocation -> ((List<?>) invocation.getArgument(1)).size());

        int modifiedCount = customerRepository.batchUpsert(customers);

        assertEquals(customers.size(), modifiedCount);
        for (Map.Entry<String, List<Customer>> entry : customersByTable.entrySet()) {
            List<CustomerDO> expectedCustomerDos = entry.getValue().stream()
                    .map(this::buildCustomerDO)
                    .toList();
            verify(customerMapper).batchUpsert(entry.getKey(), expectedCustomerDos);
        }
    }

    @Test
    @DisplayName("batchUpsert wraps mapper failure as dependency exception")
    void batchUpsert_mapperFailure_fails() {
        List<Customer> customers = List.of(
                buildCustomer("1000000001", "Alice Tan", Gender.F)
        );
        doThrow(new RuntimeException("db failure"))
                .when(customerMapper)
                .batchUpsert(anyString(), anyList());

        DependencyException exception = assertThrows(
                DependencyException.class,
                () -> customerRepository.batchUpsert(customers)
        );

        assertEquals(DependencyErrorCode.DATABASE_UPDATE_FAILED, exception.getErrorType());
    }

    @Test
    @DisplayName("selectCustomersByDates fails when startDate is null")
    void selectCustomersByDates_nullStartDate_fails() {
        ParamException exception = assertThrows(
                ParamException.class,
                () -> customerRepository.selectCustomersByDates(null, END_DATE, 10, customer -> { })
        );

        assertEquals(ParamErrorCode.NULL_PARAM, exception.getErrorType());
        verifyNoInteractions(customerMapper);
    }

    @Test
    @DisplayName("selectCustomersByDates fails when endDate is before startDate")
    void selectCustomersByDates_endDateBeforeStartDate_fails() {
        ParamException exception = assertThrows(
                ParamException.class,
                () -> customerRepository.selectCustomersByDates(END_DATE, START_DATE, 10, customer -> { })
        );

        assertEquals(ParamErrorCode.INVALID_PARAM, exception.getErrorType());
        verifyNoInteractions(customerMapper);
    }

    @Test
    @DisplayName("selectCustomersByDates merges shard results in global createdAt order")
    void selectCustomersByDates_multipleShards_mergesInCreatedAtOrder() {
        Map<String, Map<Integer, List<CustomerDO>>> shardResults = Map.of(
                "customers_0", Map.of(
                        0, List.of(
                                buildCustomerDO("1000000001", START_DATE),
                                buildCustomerDO("1000000004", START_DATE.plusDays(3))
                        ),
                        2, List.of(buildCustomerDO("1000000005", END_DATE)),
                        3, List.of()
                ),
                "customers_1", Map.of(
                        0, List.of(
                                buildCustomerDO("1000000002", START_DATE.plusDays(1)),
                                buildCustomerDO("1000000003", START_DATE.plusDays(2))
                        ),
                        2, List.of()
                )
        );
        stubShardQueries(shardResults);
        List<Customer> exportedCustomers = new ArrayList<>();

        customerRepository.selectCustomersByDates(START_DATE, END_DATE, 5, exportedCustomers::add);

        assertEquals(
                List.of("1000000001", "1000000002", "1000000003", "1000000004", "1000000005"),
                exportedCustomers.stream().map(Customer::getAccountNumber).toList()
        );
        assertIterableEquals(
                exportedCustomers.stream()
                        .map(Customer::getCreatedAt)
                        .sorted(Comparator.naturalOrder())
                        .toList(),
                exportedCustomers.stream().map(Customer::getCreatedAt).toList()
        );
        assertEquals(START_DATE, exportedCustomers.getFirst().getCreatedAt());
        assertEquals(END_DATE, exportedCustomers.getLast().getCreatedAt());
        verify(customerMapper).selectCustomersByDates("customers_0", START_DATE, END_DATE, 1000, 0);
        verify(customerMapper).selectCustomersByDates("customers_1", START_DATE, END_DATE, 1000, 0);
        verify(customerMapper).selectCustomersByDates("customers_0", START_DATE, END_DATE, 1000, 2);
        verify(customerMapper).selectCustomersByDates("customers_0", START_DATE, END_DATE, 1000, 3);
        verify(customerMapper).selectCustomersByDates("customers_1", START_DATE, END_DATE, 1000, 2);
    }

    @Test
    @DisplayName("selectCustomersByDates throws biz exception when every shard is empty")
    void selectCustomersByDates_noCustomersFound_fails() {
        stubShardQueries(Map.of());

        BizException exception = assertThrows(
                BizException.class,
                () -> customerRepository.selectCustomersByDates(START_DATE, END_DATE, 10, customer -> { })
        );

        assertEquals(BizErrorCode.CUSTOMERS_NOT_FOUND_EXPORT, exception.getErrorType());
        assertEquals(
                "Customers not found with details matching: createdAt between " + START_DATE + " and " + END_DATE,
                exception.getMessage()
        );
        verify(customerMapper).selectCustomersByDates("customers_0", START_DATE, END_DATE, 1000, 0);
        verify(customerMapper).selectCustomersByDates("customers_9", START_DATE, END_DATE, 1000, 0);
    }

    private void stubShardQueries(Map<String, Map<Integer, List<CustomerDO>>> shardResults) {
        when(customerMapper.selectCustomersByDates(anyString(), eq(START_DATE), eq(END_DATE), eq(1000), anyInt()))
                .thenAnswer(invocation -> shardResults
                        .getOrDefault(invocation.getArgument(0), Map.of())
                        .getOrDefault(invocation.getArgument(4), List.of()));
    }

    private CustomerDO buildCustomerDO(String accountNumber, LocalDateTime createdAt) {
        CustomerDO customerDO = new CustomerDO();
        customerDO.setAccountNumber(accountNumber);
        customerDO.setName("Name " + accountNumber);
        customerDO.setGender("M");
        customerDO.setCreatedAt(createdAt);
        customerDO.setModifiedAt(createdAt.plusHours(1));
        return customerDO;
    }

    private Customer buildCustomer(String accountNumber, String name, Gender gender) {
        return Customer.builder()
                .accountNumber(accountNumber)
                .name(name)
                .gender(gender)
                .build();
    }

    private CustomerDO buildCustomerDO(Customer customer) {
        CustomerDO customerDO = new CustomerDO();
        customerDO.setAccountNumber(customer.getAccountNumber());
        customerDO.setName(customer.getName());
        customerDO.setGender(customer.getGender().name());
        return customerDO;
    }
}
