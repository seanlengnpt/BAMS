package com.shopee.banking.bams.domain.aggregateRoot;

import com.shopee.banking.bams.domain.valueObject.AdminId;
import com.shopee.banking.bams.domain.valueObject.AdminNickname;
import com.shopee.banking.bams.domain.valueObject.AdminProfilePictureUrl;
import com.shopee.banking.bams.domain.valueObject.HashedPassword;
import lombok.Getter;

@Getter
public class Admin extends BaseAggregateRoot {

    String adminNickname;
    String adminProfilePictureUrl;
    String hashedPassword;
    String username;

    public Admin(Long id,
                 String username,
                 String nickname,
                 String adminProfilePictureUrl
    ){
        super(id);
        this.username = username;
        this.adminNickname = nickname;
        this.adminProfilePictureUrl = adminProfilePictureUrl;
    }

    public Admin(Long id,
                 String hashedPassword,
                 String username,
                 String nickname,
                 String adminProfilePictureUrl
    ){
        super(id);
        this.hashedPassword = hashedPassword;
        this.username = username;
        this.adminNickname = nickname;
        this.adminProfilePictureUrl = adminProfilePictureUrl;
    }


}
