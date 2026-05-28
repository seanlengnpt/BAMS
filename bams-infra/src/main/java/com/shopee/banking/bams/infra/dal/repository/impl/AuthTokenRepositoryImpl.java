package com.shopee.banking.bams.infra.dal.repository.impl;

import com.shopee.banking.bams.common.DependencyException;
import com.shopee.banking.bams.common.enums.DependencyErrorCode;
import com.shopee.banking.bams.common.enums.ParamErrorCode;
import com.shopee.banking.bams.common.util.Asserter;
import com.shopee.banking.bams.domain.JwtToken;
import com.shopee.banking.bams.domain.repository.IAuthTokenRepository;
import com.shopee.banking.bams.infra.dal.converter.AdminTokenDataConverter;
import com.shopee.banking.bams.infra.dal.dataObject.AdminTokenDO;
import com.shopee.banking.bams.infra.dal.mapper.AdminTokenMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AuthTokenRepositoryImpl implements IAuthTokenRepository {
    @Autowired
    private AdminTokenMapper adminTokenMapper;

    @Autowired
    private AdminTokenDataConverter adminTokenDataConverter;

    @Override
    public void saveRefreshToken(JwtToken refreshToken) {
        Asserter.assertNotNull(refreshToken, ParamErrorCode.NULL_PARAM, "Refresh token");
        AdminTokenDO adminTokenDO = adminTokenDataConverter.toDataObject(refreshToken);
        try {
            adminTokenMapper.insert(adminTokenDO);
        } catch (Throwable e) {
            throw new DependencyException(DependencyErrorCode.DATABASE_INSERT_FAILED, refreshToken);
        }
    }

    @Override
    public JwtToken queryByRefreshToken(String refreshToken) {
        Asserter.assertNotNull(refreshToken, ParamErrorCode.NULL_PARAM, "Refresh token");
        AdminTokenDO adminTokenDO;
        try {
            adminTokenDO = adminTokenMapper.selectByRefreshToken(refreshToken);
        } catch (Throwable e) {
            throw new DependencyException(DependencyErrorCode.DATABASE_QUERY_FAILED, refreshToken);
        }
        return adminTokenDataConverter.toEntity(adminTokenDO);
    }
}
