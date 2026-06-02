package com.shopee.banking.bams.app.service.impl;

import com.shopee.banking.bams.app.service.ICustomerService;
import com.shopee.banking.bams.app.service.dto.CreateCustomerByCsvResult;
import com.shopee.banking.bams.common.exception.BizException;
import com.shopee.banking.bams.common.exception.DependencyException;
import com.shopee.banking.bams.common.exception.ParamException;
import com.shopee.banking.bams.common.exception.enums.BizErrorCode;
import com.shopee.banking.bams.common.exception.enums.DependencyErrorCode;
import com.shopee.banking.bams.common.exception.enums.Gender;
import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;
import com.shopee.banking.bams.domain.aggregateRoot.Customer;
import com.shopee.banking.bams.infra.dal.repository.impl.CustomerRepositoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    private static final LocalDateTime START_DATE = LocalDateTime.of(2020, 5, 1, 0, 0);
    private static final LocalDateTime END_DATE = LocalDateTime.of(2020, 5, 31, 23, 59, 59);
    private static final Path EXPORT_DIRECTORY = Path.of("file-storage/exports");

    private CustomerServiceImpl customerService;
    private Set<Path> filesBeforeTest;

    @TempDir
    Path tempDir;

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
    @DisplayName("createCustomerByCSV fails when csvFilePath is blank")
    void createCustomerByCSV_blankCsvFilePath_fails() {
        ParamException exception = assertThrows(
                ParamException.class,
                () -> customerService.createCustomerByCSV("   ")
        );

        assertEquals(ParamErrorCode.INVALID_PARAM, exception.getErrorType());
        verifyNoInteractions(customerRepository);
    }

    @Test
    @DisplayName("createCustomerByCSV upserts valid CSV rows and returns modified count")
    @SuppressWarnings("unchecked")
    void createCustomerByCSV_validCsv_upsertsCustomers() throws IOException {
        Path csvFile = writeCsvFile(
                "valid-customers.csv",
                List.of(
                        "account_number,name,gender",
                        "1000000001,Alice Tan,F",
                        "1000000002,Bob Lim,M"
                )
        );
        when(customerRepository.batchUpsert(any(List.class))).thenReturn(2);

        CreateCustomerByCsvResult result = customerService.createCustomerByCSV(csvFile.toString());

        assertEquals(2, result.getModifiedCount());
        ArgumentCaptor<List<Customer>> customerBatchCaptor = ArgumentCaptor.forClass(List.class);
        verify(customerRepository).batchUpsert(customerBatchCaptor.capture());
        List<Customer> customers = customerBatchCaptor.getValue();
        assertEquals(2, customers.size());
        assertEquals("1000000001", customers.get(0).getAccountNumber());
        assertEquals("Alice Tan", customers.get(0).getName());
        assertEquals(Gender.F, customers.get(0).getGender());
        assertEquals("1000000002", customers.get(1).getAccountNumber());
        assertEquals("Bob Lim", customers.get(1).getName());
        assertEquals(Gender.M, customers.get(1).getGender());
    }

    @Test
    @DisplayName("createCustomerByCSV fails when file does not exist")
    void createCustomerByCSV_missingFile_fails() {
        BizException exception = assertThrows(
                BizException.class,
                () -> customerService.createCustomerByCSV(tempDir.resolve("missing.csv").toString())
        );

        assertEquals(BizErrorCode.INVALID_CSV_FILEPATH, exception.getErrorType());
        verifyNoInteractions(customerRepository);
    }

    @Test
    @DisplayName("createCustomerByCSV returns invalid CSV errors with line numbers")
    void createCustomerByCSV_invalidRows_failsWithLineNumbers() throws IOException {
        Path csvFile = writeCsvFile(
                "invalid-customers.csv",
                List.of(
                        "account_number,name,gender",
                        "123,Alice Tan,F",
                        "1000000002,   ,M",
                        "1000000003,Chris Tan,X",
                        "1000000004,Daisy Tan,F",
                        "1000000004,Evan Tan,M"
                )
        );

        BizException exception = assertThrows(
                BizException.class,
                () -> customerService.createCustomerByCSV(csvFile.toString())
        );

        assertEquals(BizErrorCode.INVALID_CSV_FILE, exception.getErrorType());
        assertTrue(exception.getMessage().contains("Line 2: Invalid account number."));
        assertTrue(exception.getMessage().contains("Line 3: Invalid name."));
        assertTrue(exception.getMessage().contains("Line 4: Invalid gender."));
        assertTrue(exception.getMessage().contains("Line 5: Duplicate account number."));
        assertTrue(exception.getMessage().contains("Line 6: Duplicate account number."));
        verifyNoInteractions(customerRepository);
    }

    @Test
    @DisplayName("createCustomerByCSV treats empty CSV files as invalid")
    void createCustomerByCSV_emptyFile_fails() throws IOException {
        Path csvFile = writeCsvFile("empty.csv", List.of());

        BizException exception = assertThrows(
                BizException.class,
                () -> customerService.createCustomerByCSV(csvFile.toString())
        );

        assertEquals(BizErrorCode.INVALID_CSV_FILE, exception.getErrorType());
        assertTrue(exception.getMessage().contains("Line 1: Invalid CSV header."));
        verifyNoInteractions(customerRepository);
    }

    @Test
    @DisplayName("createCustomerByCSV rolls back the transaction when database update fails midway")
    @SuppressWarnings("unchecked")
    void createCustomerByCSV_databaseFailure_rollsBackTransaction() throws IOException {
        Path csvFile = writeCsvFile("batch-failure.csv", createValidCustomerCsvLines(501));
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        TransactionStatus transactionStatus = new SimpleTransactionStatus();
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        when(customerRepository.batchUpsert(any(List.class)))
                .thenReturn(500)
                .thenThrow(new DependencyException(DependencyErrorCode.DATABASE_UPDATE_FAILED, "customers"));
        ICustomerService transactionalCustomerService = createTransactionalCustomerService(transactionManager);

        DependencyException exception = assertThrows(
                DependencyException.class,
                () -> transactionalCustomerService.createCustomerByCSV(csvFile.toString())
        );

        assertEquals(DependencyErrorCode.DATABASE_UPDATE_FAILED, exception.getErrorType());
        ArgumentCaptor<List<Customer>> customerBatchCaptor = ArgumentCaptor.forClass(List.class);
        verify(customerRepository, times(2)).batchUpsert(customerBatchCaptor.capture());
        assertEquals(List.of(500, 1), customerBatchCaptor.getAllValues().stream().map(List::size).toList());
        verify(transactionManager).rollback(transactionStatus);
        verify(transactionManager, never()).commit(any());
    }

    @Test
    @DisplayName("viewCustomerProfile returns customer details when accNo is valid")
    void viewCustomerProfile_validAccNo_returnsCustomer() {
        Customer customer = buildCustomer("1000000001", "Alice Tan", Gender.F, START_DATE, END_DATE);
        when(customerRepository.getCustomerByAccNo("1000000001")).thenReturn(customer);

        Customer result = customerService.viewCustomerProfile("1000000001");

        assertSame(customer, result);
        verify(customerRepository).getCustomerByAccNo("1000000001");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "123456789", "12345678901", "12345abcde"})
    @DisplayName("viewCustomerProfile rejects invalid accNo")
    void viewCustomerProfile_invalidAccNo_fails(String accNo) {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> customerService.viewCustomerProfile(accNo)
        );

        if (accNo == null) {
            assertInstanceOf(ParamException.class, exception);
            assertEquals(ParamErrorCode.NULL_PARAM, ((ParamException) exception).getErrorType());
        } else {
            assertInstanceOf(ParamException.class, exception);
            assertEquals(ParamErrorCode.INVALID_PARAM, ((ParamException) exception).getErrorType());
        }
        verifyNoInteractions(customerRepository);
    }

    @Test
    @DisplayName("viewCustomerProfile fails when customer does not exist")
    void viewCustomerProfile_customerNotFound_fails() {
        when(customerRepository.getCustomerByAccNo("1000000001")).thenReturn(null);

        BizException exception = assertThrows(
                BizException.class,
                () -> customerService.viewCustomerProfile("1000000001")
        );

        assertEquals(BizErrorCode.CUSTOMER_NOT_FOUND_MAPPING, exception.getErrorType());
        assertTrue(exception.getMessage().contains("1000000001"));
        verify(customerRepository).getCustomerByAccNo("1000000001");
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

    private Path writeCsvFile(String fileName, List<String> lines) throws IOException {
        Path csvFile = tempDir.resolve(fileName);
        Files.write(csvFile, lines);
        return csvFile;
    }

    private List<String> createValidCustomerCsvLines(int customerCount) {
        List<String> lines = new ArrayList<>(customerCount + 1);
        lines.add("account_number,name,gender");
        IntStream.rangeClosed(1, customerCount)
                .mapToObj(index -> String.format(
                        "%010d,Customer %d,%s",
                        index,
                        index,
                        index % 2 == 0 ? Gender.F.name() : Gender.M.name()
                ))
                .forEach(lines::add);
        return lines;
    }

    private ICustomerService createTransactionalCustomerService(PlatformTransactionManager transactionManager) {
        ProxyFactory proxyFactory = new ProxyFactory(customerService);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAdvice(new TransactionInterceptor(
                transactionManager,
                new AnnotationTransactionAttributeSource()
        ));
        return (ICustomerService) proxyFactory.getProxy();
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
