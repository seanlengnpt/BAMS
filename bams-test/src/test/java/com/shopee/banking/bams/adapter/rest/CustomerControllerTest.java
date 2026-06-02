package com.shopee.banking.bams.adapter.rest;

import com.shopee.banking.bams.api.api.request.CreateCustomersByCsvRequest;
import com.shopee.banking.bams.api.api.request.ExportCSVByDatesRequest;
import com.shopee.banking.bams.api.api.request.ViewCustomerProfileRequest;
import com.shopee.banking.bams.api.api.response.CreateCustomersByCsvResponse;
import com.shopee.banking.bams.api.api.response.ViewCustomerProfileResponse;
import com.shopee.banking.bams.common.exception.BizException;
import com.shopee.banking.bams.app.service.ICustomerService;
import com.shopee.banking.bams.app.service.dto.CreateCustomerByCsvResult;
import com.shopee.banking.bams.common.exception.ParamException;
import com.shopee.banking.bams.common.exception.enums.BizErrorCode;
import com.shopee.banking.bams.common.exception.enums.Gender;
import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;
import com.shopee.banking.bams.common.result.Result;
import com.shopee.banking.bams.domain.aggregateRoot.Customer;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    private static final String CSV_FILE_PATH = "/path/to/file.csv";
    private static final String EXPORT_CSV_FILE_PATH = "file-storage/exports/customers_20200102_100000.csv";
    private static final LocalDateTime START_DATE = LocalDateTime.of(2020, 5, 1, 0, 0);
    private static final LocalDateTime END_DATE = LocalDateTime.of(2020, 5, 31, 23, 59, 59);

    private CustomerController customerController;

    @Mock
    private ICustomerService customerService;

    @BeforeEach
    void setUp() {
        customerController = new CustomerController();
        ReflectionTestUtils.setField(customerController, "customerService", customerService);
    }

    @Test
    @DisplayName("createCustomersByCsv maps request and returns modified count")
    void createCustomersByCsv_validRequest_succeeds() {
        CreateCustomersByCsvRequest request = buildCreateCustomersByCsvRequest(CSV_FILE_PATH);
        when(customerService.createCustomerByCSV(CSV_FILE_PATH)).thenReturn(new CreateCustomerByCsvResult(3));

        Result<CreateCustomersByCsvResponse> result = customerController.createCustomersByCsv(request);

        assertEquals(Result.SUCCESS_CODE, result.getCode());
        assertEquals(Result.SUCCESS_MSG, result.getMsg());
        assertEquals(3, result.getData().getModifiedCount());
        verify(customerService).createCustomerByCSV(CSV_FILE_PATH);
    }

    @Test
    @DisplayName("createCustomersByCsv fails when csvFilePath is blank")
    void createCustomersByCsv_blankCsvFilePath_fails() {
        CreateCustomersByCsvRequest request = buildCreateCustomersByCsvRequest("   ");

        ParamException exception = assertThrows(
                ParamException.class,
                () -> customerController.createCustomersByCsv(request)
        );

        assertEquals(ParamErrorCode.INVALID_PARAM, exception.getErrorType());
        verify(customerService, never()).createCustomerByCSV(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("viewCustomerProfile returns customer profile details for valid accNo")
    void viewCustomerProfile_validAccNo_succeeds() {
        ViewCustomerProfileRequest request = buildViewCustomerProfileRequest("1000000001");
        LocalDateTime createdAt = LocalDateTime.of(2020, 5, 1, 8, 30);
        LocalDateTime modifiedAt = LocalDateTime.of(2020, 5, 2, 9, 45);
        Customer customer = Customer.builder()
                .id(1L)
                .accountNumber("1000000001")
                .name("Alice Tan")
                .gender(Gender.F)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .build();
        when(customerService.viewCustomerProfile("1000000001")).thenReturn(customer);

        Result<ViewCustomerProfileResponse> result = customerController.viewCustomerProfile(request);

        assertEquals(Result.SUCCESS_CODE, result.getCode());
        assertEquals(Result.SUCCESS_MSG, result.getMsg());
        assertEquals(1L, result.getData().getId());
        assertEquals("1000000001", result.getData().getAccountNumber());
        assertEquals("Alice Tan", result.getData().getName());
        assertEquals(Gender.F, result.getData().getGender());
        assertEquals(createdAt, result.getData().getCreatedAt());
        assertEquals(modifiedAt, result.getData().getModifiedAt());
        verify(customerService).viewCustomerProfile("1000000001");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "123456789", "12345678901", "12345abcde"})
    @DisplayName("viewCustomerProfile fails when accNo is invalid")
    void viewCustomerProfile_invalidAccNo_fails(String accNo) {
        ViewCustomerProfileRequest request = buildViewCustomerProfileRequest(accNo);

        ParamException exception = assertThrows(
                ParamException.class,
                () -> customerController.viewCustomerProfile(request)
        );

        assertEquals(ParamErrorCode.INVALID_PARAM, exception.getErrorType());
        verify(customerService, never()).viewCustomerProfile(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("viewCustomerProfile propagates customer not found error")
    void viewCustomerProfile_customerNotFound_fails() {
        ViewCustomerProfileRequest request = buildViewCustomerProfileRequest("1000000001");
        when(customerService.viewCustomerProfile("1000000001"))
                .thenThrow(new BizException(BizErrorCode.CUSTOMER_NOT_FOUND_MAPPING, "1000000001"));

        BizException exception = assertThrows(
                BizException.class,
                () -> customerController.viewCustomerProfile(request)
        );

        assertEquals(BizErrorCode.CUSTOMER_NOT_FOUND_MAPPING, exception.getErrorType());
        verify(customerService).viewCustomerProfile("1000000001");
    }

    @Test
    @DisplayName("exportCsvByDates delegates valid dates and returns file path")
    void exportCsvByDates_validDates_succeeds() {
        ExportCSVByDatesRequest request = buildExportCSVByDatesRequest(START_DATE, END_DATE);
        when(customerService.exportCustomersByDates(START_DATE, END_DATE)).thenReturn(EXPORT_CSV_FILE_PATH);

        Result<String> result = customerController.exportCsvByDates(request);

        assertEquals(Result.SUCCESS_CODE, result.getCode());
        assertEquals(Result.SUCCESS_MSG, result.getMsg());
        assertEquals(EXPORT_CSV_FILE_PATH, result.getData());
        verify(customerService).exportCustomersByDates(START_DATE, END_DATE);
    }

    @Test
    @DisplayName("exportCsvByDates allows equal startDate and endDate")
    void exportCsvByDates_equalStartDateAndEndDate_succeeds() {
        ExportCSVByDatesRequest request = buildExportCSVByDatesRequest(START_DATE, START_DATE);
        when(customerService.exportCustomersByDates(START_DATE, START_DATE)).thenReturn(EXPORT_CSV_FILE_PATH);

        Result<String> result = customerController.exportCsvByDates(request);

        assertEquals(Result.SUCCESS_CODE, result.getCode());
        assertEquals(EXPORT_CSV_FILE_PATH, result.getData());
        verify(customerService).exportCustomersByDates(START_DATE, START_DATE);
    }

    @Test
    @DisplayName("exportCsvByDates fails when startDate is null")
    void exportCsvByDates_nullStartDate_fails() {
        ExportCSVByDatesRequest request = buildExportCSVByDatesRequest(null, END_DATE);

        ParamException exception = assertThrows(ParamException.class, () -> customerController.exportCsvByDates(request));

        assertEquals(ParamErrorCode.INVALID_PARAM, exception.getErrorType());
        verify(customerService, never()).exportCustomersByDates(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("exportCsvByDates fails when endDate is null")
    void exportCsvByDates_nullEndDate_fails() {
        ExportCSVByDatesRequest request = buildExportCSVByDatesRequest(START_DATE, null);

        ParamException exception = assertThrows(ParamException.class, () -> customerController.exportCsvByDates(request));

        assertEquals(ParamErrorCode.INVALID_PARAM, exception.getErrorType());
        verify(customerService, never()).exportCustomersByDates(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("exportCsvByDates fails when endDate is before startDate")
    void exportCsvByDates_endDateBeforeStartDate_fails() {
        ExportCSVByDatesRequest request = buildExportCSVByDatesRequest(END_DATE, START_DATE);

        ParamException exception = assertThrows(ParamException.class, () -> customerController.exportCsvByDates(request));

        assertEquals(ParamErrorCode.INVALID_PARAM, exception.getErrorType());
        verify(customerService, never()).exportCustomersByDates(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    private CreateCustomersByCsvRequest buildCreateCustomersByCsvRequest(String csvFilePath) {
        CreateCustomersByCsvRequest request = new CreateCustomersByCsvRequest();
        ReflectionTestUtils.setField(request, "csvFilePath", csvFilePath);
        return request;
    }

    private ViewCustomerProfileRequest buildViewCustomerProfileRequest(String accNo) {
        ViewCustomerProfileRequest request = new ViewCustomerProfileRequest();
        ReflectionTestUtils.setField(request, "accNo", accNo);
        return request;
    }

    private ExportCSVByDatesRequest buildExportCSVByDatesRequest(LocalDateTime startDate, LocalDateTime endDate) {
        ExportCSVByDatesRequest request = new ExportCSVByDatesRequest();
        ReflectionTestUtils.setField(request, "startDate", startDate);
        ReflectionTestUtils.setField(request, "endDate", endDate);
        return request;
    }
}
