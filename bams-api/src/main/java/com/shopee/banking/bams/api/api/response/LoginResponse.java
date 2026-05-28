package com.shopee.banking.bams.api.api.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
}
