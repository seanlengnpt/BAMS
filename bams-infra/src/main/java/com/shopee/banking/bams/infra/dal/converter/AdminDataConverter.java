package com.shopee.banking.bams.infra.dal.converter;

import com.shopee.banking.bams.domain.aggregateRoot.Admin;
import com.shopee.banking.bams.infra.dal.dataObject.AdminDO;
import org.springframework.stereotype.Component;

@Component
public class AdminDataConverter implements BaseDataConverter<Admin, AdminDO>{
    @Override
    public Admin toEntity(AdminDO adminDO) {
        if (adminDO == null){
            return null;
        }
        Admin admin = Admin.builder()
                .id(adminDO.getId())
                .hashedPassword(adminDO.getPassword())
                .username(adminDO.getUsername())
                .adminNickname(adminDO.getNickname())
                .adminProfilePictureUrl(adminDO.getProfilePictureUrl())
                .createdAt(adminDO.getCreatedAt())
                .modifiedAt(adminDO.getModifiedAt())
                .build();
        admin.setVersion(adminDO.getVersion());
        return admin;
    }

    @Override
    public AdminDO toDataObject(Admin baseEntity) {
        return null;
    }
}
