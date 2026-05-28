package com.shopee.banking.bams.infra.dal.repository.impl;

import com.shopee.banking.bams.common.DependencyException;
import com.shopee.banking.bams.common.enums.DependencyErrorCode;
import com.shopee.banking.bams.common.enums.ParamErrorCode;
import com.shopee.banking.bams.common.util.Asserter;
import com.shopee.banking.bams.domain.aggregateRoot.Admin;
import com.shopee.banking.bams.domain.repository.IAdminRepository;
import com.shopee.banking.bams.domain.valueObject.AdminId;
import com.shopee.banking.bams.infra.dal.converter.AdminDataConverter;
import com.shopee.banking.bams.infra.dal.dataObject.AdminDO;
import com.shopee.banking.bams.infra.dal.mapper.AdminMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AdminRepositoryImpl implements IAdminRepository {
    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private AdminDataConverter adminDataConverter;

    @Override
    public Admin queryById(AdminId id) {
        Asserter.assertNotNull(id, ParamErrorCode.NULL_PARAM, "Admin Id");
        AdminDO adminDO;
        try {
            adminDO = adminMapper.selectById(id.getId());
        } catch (Throwable e) {
            throw new DependencyException(DependencyErrorCode.DATABASE_QUERY_FAILED, id);
        }
        return adminDataConverter.toEntity(adminDO);
    }

    @Override
    public Admin queryByUsername(String username) {
        Asserter.assertNotNull(username, ParamErrorCode.NULL_PARAM, "Username");
        AdminDO adminDO;
        try {
            adminDO = adminMapper.selectByUsername(username);
        } catch (Throwable e) {
            throw new DependencyException(DependencyErrorCode.DATABASE_QUERY_FAILED, username);
        }
        return adminDataConverter.toEntity(adminDO);
    }

    @Override
    public int updateProfile(AdminId id, String nickname, String profilePictureUrl) {
        Asserter.assertNotNull(id, ParamErrorCode.NULL_PARAM, "Admin Id");
        try {
            return adminMapper.updateProfile(id.getId(), nickname, profilePictureUrl);
        } catch (Throwable e) {
            throw new DependencyException(DependencyErrorCode.DATABASE_UPDATE_FAILED, id);
        }
    }
}
