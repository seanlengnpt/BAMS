package com.shopee.banking.bams.adapter.rest;

import com.shopee.banking.bams.adapter.converter.ResponseAssembler;
import com.shopee.banking.bams.api.api.request.CreateCustomersByCsvRequest;
import com.shopee.banking.bams.api.api.request.ExportCSVByDatesRequest;
import com.shopee.banking.bams.api.api.request.ViewCustomerProfileRequest;
import com.shopee.banking.bams.api.api.response.CreateCustomersByCsvResponse;
import com.shopee.banking.bams.api.api.response.ViewCustomerProfileResponse;
import com.shopee.banking.bams.app.service.ICustomerService;
import com.shopee.banking.bams.app.service.dto.CreateCustomerByCsvResult;
import com.shopee.banking.bams.common.enums.ParamErrorCode;
import com.shopee.banking.bams.common.result.Result;
import com.shopee.banking.bams.common.util.Asserter;
import com.shopee.banking.bams.common.util.ValidationUtils;
import com.shopee.banking.bams.domain.aggregateRoot.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private ICustomerService customerService;

    @PostMapping("/create-by-csv")
    public Result<CreateCustomersByCsvResponse> createCustomersByCsv(@RequestBody CreateCustomersByCsvRequest request) {
        ValidationUtils.validate(request);
        CreateCustomerByCsvResult result = customerService.createCustomerByCSV(request.getCsvFilePath());
        CreateCustomersByCsvResponse response = ResponseAssembler.assemble(result);
        return Result.success(response);
    }

    @PostMapping("/view-profile")
    public Result<ViewCustomerProfileResponse> viewCustomerProfile(@RequestBody ViewCustomerProfileRequest request){
        ValidationUtils.validate(request);
        Customer customer = customerService.viewCustomerProfile(request.getAccNo());
        return Result.success(ResponseAssembler.assemble(customer));
    }

    @PostMapping("/export-csv")
    public Result<String> exportCsvByDates(@RequestBody ExportCSVByDatesRequest request){
        ValidationUtils.validate(request);
        Asserter.assertTrue(request.getStartDate().isBefore(request.getEndDate()), ParamErrorCode.INVALID_PARAM, "start_date, end_date");
        String csvFilePath = customerService.exportCustomersByDates(request.getStartDate(), request.getEndDate());
        return Result.success(csvFilePath);
    }
}
