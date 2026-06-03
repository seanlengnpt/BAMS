package com.shopee.banking.bams.api.api.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ViewAdminProfileResponse {
    private Long id;
    private Long version;
    private String username;
    private String nickname;
    private String profilePictureUrl;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
