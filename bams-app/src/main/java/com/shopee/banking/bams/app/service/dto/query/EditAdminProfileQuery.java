package com.shopee.banking.bams.app.service.dto.query;

import lombok.Getter;
import lombok.Setter;
import com.shopee.banking.bams.domain.valueObject.AdminId;

@Getter
@Setter
public class EditAdminProfileQuery {
    private AdminId adminId;
    private String nickname;
    private String profilePictureUrl;
    private Long version;
}
