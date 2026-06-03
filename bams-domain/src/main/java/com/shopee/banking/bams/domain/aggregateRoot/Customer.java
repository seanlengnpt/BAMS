package com.shopee.banking.bams.domain.aggregateRoot;

import com.shopee.banking.bams.common.enums.Gender;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Builder
@Getter
public class Customer extends BaseAggregateRoot {
    Long id;
    String accountNumber;
    String name;
    Gender gender;
    LocalDateTime createdAt;
    LocalDateTime modifiedAt;

    @Override
    public String toString() {
        return "Account number: " + this.accountNumber + ", name: " + this.name;
    }
}
