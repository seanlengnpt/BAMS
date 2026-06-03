package com.shopee.banking.bams.domain.aggregateRoot;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class Admin extends BaseAggregateRoot {

    Long id;
    String adminNickname;
    String adminProfilePictureUrl;
    String hashedPassword;
    String username;
    LocalDateTime createdAt;
    LocalDateTime modifiedAt;
}
