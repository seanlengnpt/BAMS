package com.shopee.banking.bams.adapter.rest;

import com.shopee.banking.bams.adapter.converter.ResponseAssembler;
import com.shopee.banking.bams.api.api.request.LoginRequest;
import com.shopee.banking.bams.api.api.request.RefreshTokenRequest;
import com.shopee.banking.bams.api.api.response.LoginResponse;
import com.shopee.banking.bams.api.api.response.RefreshTokenResponse;
import com.shopee.banking.bams.app.service.IAuthService;
import com.shopee.banking.bams.app.service.dto.AuthTokens;
import com.shopee.banking.bams.common.result.Result;
import com.shopee.banking.bams.common.util.ValidationUtils;
import com.shopee.banking.bams.domain.aggregateRoot.JwtToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private IAuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        ValidationUtils.validate(request);
        AuthTokens authTokens = authService.login(request.username, request.password);
        return Result.success(ResponseAssembler.assemble(authTokens));
    }

    @PostMapping("/refresh")
    public Result<RefreshTokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
        ValidationUtils.validate(request);
        JwtToken accessToken = authService.refresh(request.getRefreshToken());
        return Result.success(ResponseAssembler.assemble(accessToken));
    }
}
