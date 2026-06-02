//package com.shopee.banking.bams.app.service.impl;
//
//import com.shopee.banking.bams.app.service.dto.CreateCustomerByCsvResult;
//import com.shopee.banking.bams.app.service.dto.query.CreateCustomerByCsvQuery;
//import com.shopee.banking.bams.common.BizException;
//import com.shopee.banking.bams.common.enums.BizErrorCode;
//import com.shopee.banking.bams.domain.aggregateRoot.Customer;
//import com.shopee.banking.bams.domain.repository.ICustomerRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.jupiter.api.io.TempDir;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.anyCollection;
//import static org.mockito.ArgumentMatchers.anyList;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoInteractions;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class CustomerServiceImplTest {
//
//    private CustomerServiceImpl customerService;
//
//    @Mock
//    private ICustomerRepository customerRepository;
//
//    @TempDir
//    private Path tempDir;
//
//    @BeforeEach
//    void setUp() {
//        customerService = new CustomerServiceImpl();
//        ReflectionTestUtils.setField(customerService, "customerRepository", customerRepository);
//    }
//
//    @Test
//    @DisplayName("createCustomerByCSV fails when csv filepath is invalid")
//    void createCustomerByCSV_invalidFilepath_throwsBizException() {
//        BizException exception = assertThrows(BizException.class, () -> customerService.createCustomerByCSV("missing.csv"));
//        assertEquals(BizErrorCode.INVALID_CSV_FILEPATH, exception.getErrorType());
//        verifyNoInteractions(customerRepository);
//    }
//
//    @Test
//    @DisplayName("createCustomerByCSV inserts valid CSV rows and returns count")
//    @SuppressWarnings("unchecked")
//    void createCustomerByCSV_validCsv_insertsRows() throws IOException {
//        Path csvFile = writeCsv("""
//                account_number,name,gender
//                1234567890,"Jane, Lee",F
//                0987654321,John Tan,M
//                """);
//        when(customerRepository.batchInsert(anyList())).thenReturn(2);
//
//        CreateCustomerByCsvResult result = customerService.createCustomerByCSV(csvFile);
//
//        assertEquals(2, result.getCreatedCount());
//        assertEquals(List.of(), result.getErrors());
//        ArgumentCaptor<List<Customer>> customersCaptor = ArgumentCaptor.forClass(List.class);
//        verify(customerRepository).batchInsert(customersCaptor.capture());
//        assertEquals("1234567890", customersCaptor.getValue().get(0).getAccountNumber());
//        assertEquals("Jane, Lee", customersCaptor.getValue().get(0).getName());
//    }
//
//    @Test
//    @DisplayName("createCustomerByCSV returns line number for invalid account number")
//    void createCustomerByCSV_invalidAccountNumber_returnsLineNumber() throws IOException {
//        Path csvFile = writeCsv("""
//                account_number,name,gender
//                12345,Jane Lee,F
//                """);
//
//        CreateCustomerByCsvResult result = customerService.createCustomerByCSV(buildQuery(csvFile));
//
//        assertEquals(0, result.getCreatedCount());
//        assertEquals(List.of("Line 2: Invalid account number."), result.getErrors());
//        verifyNoInteractions(customerRepository);
//    }
//
//    @Test
//    @DisplayName("createCustomerByCSV returns line number for invalid gender")
//    void createCustomerByCSV_invalidGender_returnsLineNumber() throws IOException {
//        Path csvFile = writeCsv("""
//                account_number,name,gender
//                1234567890,Jane Lee,UNKNOWN
//                """);
//
//        CreateCustomerByCsvResult result = customerService.createCustomerByCSV(buildQuery(csvFile));
//
//        assertEquals(List.of("Line 2: Invalid gender."), result.getErrors());
//        verifyNoInteractions(customerRepository);
//    }
//
//    @Test
//    @DisplayName("createCustomerByCSV returns line numbers for blank and too-long names")
//    void createCustomerByCSV_invalidNames_returnsLineNumbers() throws IOException {
//        Path csvFile = writeCsv("""
//                account_number,name,gender
//                1234567890,   ,F
//                0987654321,%s,M
//                """.formatted("a".repeat(251)));
//
//        CreateCustomerByCsvResult result = customerService.createCustomerByCSV(buildQuery(csvFile));
//
//        assertEquals(List.of("Line 2: Invalid name.", "Line 3: Invalid name."), result.getErrors());
//        verifyNoInteractions(customerRepository);
//    }
//
//    @Test
//    @DisplayName("createCustomerByCSV returns every duplicate account number line in CSV")
//    void createCustomerByCSV_duplicateAccountNumbers_returnsAllDuplicateLineNumbers() throws IOException {
//        Path csvFile = writeCsv("""
//                account_number,name,gender
//                1234567890,Jane Lee,F
//                0987654321,John Tan,M
//                1234567890,Mary Ng,OTHERS
//                """);
//
//        CreateCustomerByCsvResult result = customerService.createCustomerByCSV(buildQuery(csvFile));
//
//        assertEquals(List.of("Line 2: Duplicate account number.", "Line 4: Duplicate account number."), result.getErrors());
//        verifyNoInteractions(customerRepository);
//    }
//
//    @Test
//    @DisplayName("createCustomerByCSV upserts rows when account number already exists")
//    void createCustomerByCSV_existingDbAccount_upsertsRows() throws IOException {
//        Path csvFile = writeCsv("""
//                account_number,name,gender
//                1234567890,Jane Lee,F
//                0987654321,John Tan,M
//                """);
//        when(customerRepository.batchInsert(anyList())).thenReturn(2);
//
//        CreateCustomerByCsvResult result = customerService.createCustomerByCSV(buildQuery(csvFile));
//
//        assertEquals(2, result.getCreatedCount());
//        assertEquals(List.of(), result.getErrors());
//        verify(customerRepository, never()).findExistingAccountNumbers(anyCollection());
//        verify(customerRepository).batchInsert(anyList());
//    }
//
//    private Path writeCsv(String content) throws IOException {
//        Path csvFile = Files.createTempFile(tempDir, "customers", ".csv");
//        Files.writeString(csvFile, content);
//        return csvFile;
//    }
//}
