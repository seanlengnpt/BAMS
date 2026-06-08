package com.shopee.banking.bams.adapter.rest;

import com.shopee.banking.bams.adapter.converter.ResponseAssembler;
import com.shopee.banking.bams.api.api.request.EditAdminProfileRequest;
import com.shopee.banking.bams.api.api.request.SendAdminMessageRequest;
import com.shopee.banking.bams.api.api.request.ViewAdminProfileRequest;
import com.shopee.banking.bams.api.api.response.EditAdminProfileResponse;
import com.shopee.banking.bams.api.api.response.ViewAdminProfileResponse;
import com.shopee.banking.bams.app.service.IAdminService;
import com.shopee.banking.bams.adapter.converter.QueryBuilder;
import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;
import com.shopee.banking.bams.common.result.Result;
import com.shopee.banking.bams.common.util.Asserter;
import com.shopee.banking.bams.domain.aggregateRoot.Admin;
import com.shopee.banking.bams.infra.mq.message.AdminMessage;
import com.shopee.banking.bams.infra.mq.producer.AdminMessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.shopee.banking.bams.common.util.ValidationUtils;

import java.time.LocalDateTime;
import java.util.UUID;


@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private IAdminService adminService;

    @Autowired
    private AdminMessageProducer adminMessageProducer;

    @PostMapping("/view-profile")
    public Result<ViewAdminProfileResponse> getAdminProfile(@RequestBody ViewAdminProfileRequest request){

        ValidationUtils.validate(request);
        Admin admin = adminService.getAdminById(QueryBuilder.build(request));
        return Result.success(ResponseAssembler.assemble(admin));
    }

    @PostMapping
    public Result<EditAdminProfileResponse> editAdminProfile(@RequestBody EditAdminProfileRequest request) {
        ValidationUtils.validate(request);
        Asserter.assertTrue(!(request.getNickname() == null && request.getProfilePictureUrl() == null), ParamErrorCode.NULL_PARAM);
        int modifiedCount = adminService.editAdminProfile(QueryBuilder.build(request));
        return Result.success(new EditAdminProfileResponse(modifiedCount));
    }

    @PostMapping("/message")
    public Result<Void> sendAdminMessage(@RequestBody SendAdminMessageRequest request) {
        ValidationUtils.validate(request);
        adminMessageProducer.send(AdminMessage.builder()
                .adminId(request.getAdminId())
                .eventId(UUID.randomUUID().toString())
                .occurredAt(LocalDateTime.now())
                .build());
        return Result.success();
    }
}
