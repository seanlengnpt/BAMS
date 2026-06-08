package com.shopee.banking.bams.infra.mq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMessage {
    private Long adminId;
    private String eventId;
    private LocalDateTime occurredAt;
}
