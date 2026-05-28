package com.shopee.banking.bams.app.service;

import com.shopee.banking.bams.app.service.dto.AuthTokens;
import com.shopee.banking.bams.domain.JwtToken;
import com.shopee.banking.bams.domain.valueObject.AdminId;

public interface IAuthService {
    AuthTokens login(String username, String password);

    JwtToken refresh(String refreshToken);

    AdminId verifyAccessToken(String accessToken);
}
