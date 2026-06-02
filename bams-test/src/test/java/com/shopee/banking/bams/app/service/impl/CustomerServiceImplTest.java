package com.shopee.banking.bams.app.service.impl;

import com.shopee.banking.bams.common.exception.BizException;
import com.shopee.banking.bams.common.exception.ParamException;
import com.shopee.banking.bams.common.exception.enums.BizErrorCode;
import com.shopee.banking.bams.common.exception.enums.Gender;
import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;
import com.shopee.banking.bams.domain.aggregateRoot.Customer;
import com.shopee.banking.bams.infra.dal.repository.impl.CustomerRepositoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    private static final LocalDateTime START_DATE = LocalDateTime.of(2020, 5, 1, 0, 0);
    private static final LocalDateTime END_DATE = LocalDateTime.of(2020, 5, 31, 23, 59, 59);
    private static final Path EXPORT_DIRECTORY = Path.of("file-storage/exports");

    private CustomerServiceImpl customerService;
    private Set<Path> filesBeforeTest;

    @Mock
    private CustomerRepositoryImpl customerRepository;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl();
        ReflectionTestUtils.setField(customerService, "customerRepository", customerRepository);
        filesBeforeTest = listExportedFilesUnchecked();
    }

    @AfterEach
    void cleanUpExportedFiles() throws IOException {
        Set<Path> filesAfterTest = listExportedFiles();
        List<Path> generatedFiles = new ArrayList<>(filesAfterTest);
        generatedFiles.removeAll(filesBeforeTest);
        for (Path generatedFile : generatedFiles) {
            Files.deleteIfExists(generatedFile);
        }
    }

    @Test
    @DisplayName("exportCustomersByDates fails when startDate is null")
    void exportCustomersByDates_nullStartDate_fails() {
        ParamException exception = assertThrows(
                ParamException.class,
                () -> customerService.exportCustomersByDates(null, END_DATE)
        );

        assertEquals(ParamErrorCode.NULL_PARAM, exception.getErrorType());
        verifyNoInteractions(customerRepository);
    }

    @Test
    @DisplayName("exportCustomersByDates fails when endDate is null")
    void exportCustomersByDates_nullEndDate_fails() {
        ParamException exception = assertThrows(
                ParamException.class,
                () -> customerService.exportCustomersByDates(START_DATE, null)
        );

        assertEquals(ParamErrorCode.NULL_PARAM, exception.getErrorType());
        verifyNoInteractions(customerRepository);
    }

    @Test
    @DisplayName("exportCustomersByDates writes CSV header and customer rows returned by repository")
    @SuppressWarnings("unchecked")
    void exportCustomersByDates_validDates_writesCsv() throws IOException {
        List<Customer> customers = List.of(
                buildCustomer("1000000001", "Alice Tan", Gender.F, START_DATE, START_DATE.plusDays(1)),
                buildCustomer("1000000002", "Bob Lim", Gender.M, START_DATE.plusDays(2), START_DATE.plusDays(3)),
                buildCustomer("1000000003", "Casey Ng", Gender.OTHERS, END_DATE.minusDays(1), END_DATE)
        );
        doAnswer(invocation -> {
            Consumer<Customer> consumer = invocation.getArgument(3);
            customers.forEach(consumer);
            return null;
        }).when(customerRepository).selectCustomersByDates(eq(START_DATE), eq(END_DATE), eq(10000), any(Consumer.class));

        String csvFilePath = customerService.exportCustomersByDates(START_DATE, END_DATE);

        Path exportedFile = Path.of(csvFilePath);
        assertTrue(Files.exists(exportedFile));
        assertTrue(csvFilePath.matches("file-storage/exports/customers_\\d{8}_\\d{6}\\.csv"));
        assertLinesMatch(
                List.of(
                        "account_number,name,gender,created_at,modified_at",
                        "1000000001,Alice Tan,F,01/05/2020,02/05/2020",
                        "1000000002,Bob Lim,M,03/05/2020,04/05/2020",
                        "1000000003,Casey Ng,OTHERS,30/05/2020,31/05/2020"
                ),
                Files.readAllLines(exportedFile)
        );

        ArgumentCaptor<Consumer<Customer>> consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(customerRepository).selectCustomersByDates(eq(START_DATE), eq(END_DATE), eq(10000), consumerCaptor.capture());
        assertInstanceOf(Consumer.class, consumerCaptor.getValue());
    }

    @Test
    @DisplayName("exportCustomersByDates propagates customers not found biz exception")
    void exportCustomersByDates_noCustomersFound_fails() throws IOException {
        Set<Path> filesBeforeCall = listExportedFiles();
        doThrow(new BizException(BizErrorCode.CUSTOMERS_NOT_FOUND_EXPORT, "createdAt between " + START_DATE + " and " + END_DATE))
                .when(customerRepository)
                .selectCustomersByDates(eq(START_DATE), eq(END_DATE), eq(10000), any(Consumer.class));

        BizException exception = assertThrows(
                BizException.class,
                () -> customerService.exportCustomersByDates(START_DATE, END_DATE)
        );

        assertEquals(BizErrorCode.CUSTOMERS_NOT_FOUND_EXPORT, exception.getErrorType());
        assertTrue(exception.getMessage().contains("Customers not found"));
        verify(customerRepository).selectCustomersByDates(eq(START_DATE), eq(END_DATE), eq(10000), any(Consumer.class));

        Set<Path> filesAfterCall = listExportedFiles();
        List<Path> leakedFiles = new ArrayList<>(filesAfterCall);
        leakedFiles.removeAll(filesBeforeCall);
        for (Path leakedFile : leakedFiles) {
            Files.deleteIfExists(leakedFile);
        }
    }

    private Set<Path> listExportedFilesUnchecked() {
        try {
            return listExportedFiles();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<Path> listExportedFiles() throws IOException {
        if (!Files.exists(EXPORT_DIRECTORY)) {
            return Set.of();
        }
        try (var paths = Files.list(EXPORT_DIRECTORY)) {
            return paths.collect(Collectors.toSet());
        }
    }

    private Customer buildCustomer(String accountNumber,
                                   String name,
                                   Gender gender,
                                   LocalDateTime createdAt,
                                   LocalDateTime modifiedAt) {
        return Customer.builder()
                .accountNumber(accountNumber)
                .name(name)
                .gender(gender)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .build();
    }
}
