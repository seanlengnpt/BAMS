package com.shopee.banking.bams.adapter.converter;

import com.shopee.banking.bams.api.api.request.EditAdminProfileRequest;
import com.shopee.banking.bams.api.api.request.ViewAdminProfileRequest;
import com.shopee.banking.bams.app.service.dto.query.AdminProfileQuery;
import com.shopee.banking.bams.app.service.dto.query.EditAdminProfileQuery;
import com.shopee.banking.bams.domain.valueObject.AdminId;

public class QueryBuilder {

    public static AdminProfileQuery build(ViewAdminProfileRequest request){
        AdminProfileQuery query = new AdminProfileQuery();
        query.setAdminId(new AdminId(request.id));
        return query;
    }

    public static EditAdminProfileQuery build(EditAdminProfileRequest request){
        EditAdminProfileQuery query = new EditAdminProfileQuery();
        query.setAdminId(new AdminId(request.getId()));
        query.setNickname(request.getNickname());
        query.setProfilePictureUrl(request.getProfilePictureUrl());
        return query;
    }

}
