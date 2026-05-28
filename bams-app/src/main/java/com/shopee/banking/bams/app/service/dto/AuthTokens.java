package com.shopee.banking.bams.app.service.dto;

import com.shopee.banking.bams.domain.JwtToken;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthTokens {
    private JwtToken accessToken;
    private JwtToken refreshToken;
}
