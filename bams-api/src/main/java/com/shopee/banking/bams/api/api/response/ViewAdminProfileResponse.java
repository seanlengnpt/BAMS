package com.shopee.banking.bams.api.api.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViewAdminProfileResponse {
    private Long id;
    private String username;
    private String nickname;
    private String profilePictureUrl;
}
