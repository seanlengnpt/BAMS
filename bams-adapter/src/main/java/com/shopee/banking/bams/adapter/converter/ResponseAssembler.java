package com.shopee.banking.bams.adapter.converter;

import com.shopee.banking.bams.api.api.response.*;
import com.shopee.banking.bams.app.service.dto.CreateCustomerByCsvResult;
import com.shopee.banking.bams.app.service.dto.AuthTokens;
import com.shopee.banking.bams.domain.aggregateRoot.Customer;
import com.shopee.banking.bams.domain.aggregateRoot.JwtToken;
import com.shopee.banking.bams.domain.aggregateRoot.Admin;

public class ResponseAssembler {

    public static ViewAdminProfileResponse assemble(Admin admin){
        if (admin == null){
            return null;
        }
        ViewAdminProfileResponse response = new ViewAdminProfileResponse();
        response.setId(admin.getId());
        response.setNickname(admin.getAdminNickname());
        response.setUsername(admin.getUsername());
        response.setProfilePictureUrl(admin.getAdminProfilePictureUrl());
        return response;
    }

    public static LoginResponse assemble(AuthTokens authTokens){
        if (authTokens == null){
            return null;
        }
        LoginResponse response = new LoginResponse();
        response.setAccessToken(authTokens.getAccessToken().getToken());
        response.setRefreshToken(authTokens.getRefreshToken().getToken());
        return response;
    }

    public static RefreshTokenResponse assemble(JwtToken accessToken){
        if (accessToken == null){
            return null;
        }
        RefreshTokenResponse response = new RefreshTokenResponse();
        response.setAccessToken(accessToken.getToken());
        return response;
    }

    public static ViewCustomerProfileResponse assemble(Customer customer){
        if (customer == null){
            return null;
        }
        ViewCustomerProfileResponse response = new ViewCustomerProfileResponse();
        response.setName(customer.getName());
        response.setGender(customer.getGender());
        response.setId(customer.getId());
        response.setAccountNumber(customer.getAccountNumber());
        response.setCreatedAt(customer.getCreatedAt());
        return response;
    }


    public static CreateCustomersByCsvResponse assemble(CreateCustomerByCsvResult result){
        if (result == null){
            return null;
        }
        return new CreateCustomersByCsvResponse(result.getCreatedCount(), result.getModifiedCount(), result.getErrors());
    }

}
