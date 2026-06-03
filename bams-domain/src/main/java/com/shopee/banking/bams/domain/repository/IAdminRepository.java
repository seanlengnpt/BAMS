package com.shopee.banking.bams.domain.repository;

import com.shopee.banking.bams.domain.aggregateRoot.Admin;
import com.shopee.banking.bams.domain.valueObject.AdminId;

public interface IAdminRepository {
    Admin queryById(AdminId id);

    Admin selectByIdForUpdate(AdminId id);

    Admin queryByUsername(String username);

    int updateProfile(AdminId id, String nickname, String profilePictureUrl);
}
