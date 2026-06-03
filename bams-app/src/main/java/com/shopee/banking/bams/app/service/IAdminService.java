package com.shopee.banking.bams.app.service;

import com.shopee.banking.bams.app.service.dto.query.AdminProfileQuery;
import com.shopee.banking.bams.app.service.dto.query.EditAdminProfileQuery;
import com.shopee.banking.bams.domain.aggregateRoot.Admin;
import com.shopee.banking.bams.domain.valueObject.AdminId;

public interface IAdminService {
    Admin getAdminById(AdminProfileQuery query);

    int editAdminProfile(EditAdminProfileQuery query);
}
