package com.shopee.banking.bams.adapter.rest;

import com.shopee.banking.bams.api.api.request.CreateCustomersByCsvRequest;
import com.shopee.banking.bams.api.api.response.CreateCustomersByCsvResponse;
import com.shopee.banking.bams.app.service.ICustomerService;
import com.shopee.banking.bams.app.service.dto.CreateCustomerByCsvResult;
import com.shopee.banking.bams.app.service.dto.query.CreateCustomerByCsvQuery;
import com.shopee.banking.bams.common.ParamException;
import com.shopee.banking.bams.common.result.Result;
import com.shopee.banking.bams.domain.aggregateRoot.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerControllerTest {

    private static final String CSV_FILE_PATH = "/path/to/file.csv";

    private CustomerController customerController;
    private CapturingCustomerService customerService;

    @BeforeEach
    void setUp() {
        customerController = new CustomerController();
        customerService = new CapturingCustomerService();
        ReflectionTestUtils.setField(customerController, "customerService", customerService);
    }

    @Test
    @DisplayName("createCustomersByCsv maps request to query and returns created count")
    void createCustomersByCsv_validRequest_succeeds() {
        CreateCustomersByCsvRequest request = buildCreateCustomersByCsvRequest(CSV_FILE_PATH);
        customerService.result = new CreateCustomerByCsvResult(10, 3, List.of());

        Result<CreateCustomersByCsvResponse> result = customerController.createCustomersByCsv(request);

        assertEquals(Result.SUCCESS_CODE, result.getCode());
        assertEquals(Result.SUCCESS_MSG, result.getMsg());
        assertEquals(10, result.getData().getCreatedCount());
        assertEquals(List.of(), result.getData().getErrors());
    }

    @Test
    @DisplayName("createCustomersByCsv fails when csvFilePath is blank")
    void createCustomersByCsv_blankCsvFilePath_fails() {
        CreateCustomersByCsvRequest request = buildCreateCustomersByCsvRequest("   ");

        assertThrows(ParamException.class, () -> customerController.createCustomersByCsv(request));
        assertFalse(customerService.called);
    }

    private CreateCustomersByCsvRequest buildCreateCustomersByCsvRequest(String csvFilePath) {
        CreateCustomersByCsvRequest request = new CreateCustomersByCsvRequest();
        ReflectionTestUtils.setField(request, "csvFilePath", csvFilePath);
        return request;
    }

    private static class CapturingCustomerService implements ICustomerService {
        private boolean called;
        private CreateCustomerByCsvResult result;

        @Override
        public CreateCustomerByCsvResult createCustomerByCSV(String csvFilePath) {
            this.called = true;
            return result;
        }

        @Override
        public Customer viewCustomerProfile(String accNo) {
            return null;
        }

        @Override
        public String exportCustomersByDates(LocalDateTime startDate, LocalDateTime endDate) {
            return "";
        }
    }
}
