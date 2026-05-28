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
        return new Admin(adminDO.getId(), adminDO.getPassword(), adminDO.getUsername(), adminDO.getNickname(), adminDO.getProfilePictureUrl());
    }

    @Override
    public AdminDO toDataObject(Admin baseEntity) {
        return null;
    }
}
