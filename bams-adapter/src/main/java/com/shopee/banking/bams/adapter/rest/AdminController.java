package com.shopee.banking.bams.adapter.rest;

import com.shopee.banking.bams.adapter.converter.ResponseAssembler;
import com.shopee.banking.bams.api.api.request.EditAdminProfileRequest;
import com.shopee.banking.bams.api.api.request.ViewAdminProfileRequest;
import com.shopee.banking.bams.api.api.response.ViewAdminProfileResponse;
import com.shopee.banking.bams.app.service.IAdminService;
import com.shopee.banking.bams.app.service.IAuthService;
import com.shopee.banking.bams.adapter.converter.QueryBuilder;
import com.shopee.banking.bams.common.enums.AuthErrorCode;
import com.shopee.banking.bams.common.enums.ParamErrorCode;
import com.shopee.banking.bams.common.result.Result;
import com.shopee.banking.bams.common.util.Asserter;
import com.shopee.banking.bams.domain.aggregateRoot.Admin;
import com.shopee.banking.bams.domain.valueObject.AdminId;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.shopee.banking.bams.common.util.ValidationUtils;


@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private IAdminService adminService;

    @Autowired
    private IAuthService authService;

    @PostMapping("/view-profile")
    public Result<ViewAdminProfileResponse> getAdminProfile(@RequestBody ViewAdminProfileRequest request){

        ValidationUtils.validate(request);
        Admin admin = adminService.getAdminById(QueryBuilder.build(request));
        return Result.success(ResponseAssembler.assemble(admin));
    }

    @PostMapping
    public Result<ViewAdminProfileResponse> editAdminProfile(@RequestBody EditAdminProfileRequest request) {
        ValidationUtils.validate(request);
        Asserter.assertTrue(!(request.getNickname()==null && request.getProfilePictureUrl()==null), ParamErrorCode.NULL_PARAM);
        System.out.println("reached here");
        adminService.editAdminProfile(QueryBuilder.build(request));
        return Result.success();
    }

//    private String extractAccessToken(HttpServletRequest httpRequest) {
//        String authorization = httpRequest.getHeader("Authorization");
//        Asserter.assertTrue(
//                authorization != null && authorization.startsWith("Bearer "),
//                AuthErrorCode.INVALID_ACCESS_TOKEN
//        );
//
//        String accessToken = authorization.substring("Bearer ".length()).trim();
//        Asserter.assertTrue(!accessToken.isEmpty(), AuthErrorCode.INVALID_ACCESS_TOKEN);
//        return accessToken;
//    }
}
