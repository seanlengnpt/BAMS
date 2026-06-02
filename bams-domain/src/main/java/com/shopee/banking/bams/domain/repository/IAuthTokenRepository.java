package com.shopee.banking.bams.domain.repository;

import com.shopee.banking.bams.domain.aggregateRoot.JwtToken;

public interface IAuthTokenRepository {
    void saveRefreshToken(JwtToken refreshToken);

    JwtToken queryByRefreshToken(String refreshToken);
}
