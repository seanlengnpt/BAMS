package com.shopee.banking.bams.app.service.impl;

import com.shopee.banking.bams.app.service.IAdminService;
import com.shopee.banking.bams.app.service.dto.query.AdminProfileQuery;
import com.shopee.banking.bams.app.service.dto.query.EditAdminProfileQuery;
import com.shopee.banking.bams.common.exception.enums.BizErrorCode;
import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;
import com.shopee.banking.bams.common.util.Asserter;
import com.shopee.banking.bams.domain.aggregateRoot.Admin;
import com.shopee.banking.bams.domain.repository.IAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AdminServiceImpl implements IAdminService {

    @Autowired
    IAdminRepository adminRepository;

    @Override
    public Admin getAdminById(AdminProfileQuery query) {
        Admin admin = adminRepository.queryById(query.getAdminId());
        Asserter.assertNotNull(admin, BizErrorCode.ADMIN_NOT_FOUND_MAPPING, query.getAdminId().getId());
        return admin;
    }

    @Override
    public int editAdminProfile(EditAdminProfileQuery query) {
        Admin admin = adminRepository.selectByIdForUpdate(query.getAdminId());
        Asserter.assertNotNull(admin, BizErrorCode.ADMIN_NOT_FOUND_MAPPING, query.getAdminId().getId());
        Asserter.assertTrue(
                Objects.equals(query.getVersion(), admin.getVersion()),
                BizErrorCode.STALE_UPDATE,
                "Admin with id " + query.getAdminId().getId()
        );

        int updatedRows = adminRepository.updateProfile(query.getAdminId(), query.getNickname(), query.getProfilePictureUrl());
        Asserter.assertTrue(updatedRows == 1, ParamErrorCode.INVALID_PARAM);
        return updatedRows;
    }
}
