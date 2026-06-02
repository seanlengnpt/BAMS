package com.shopee.banking.bams.api.api.request;

import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ExportCSVByDatesRequest extends BaseAppRequest{
    @PastOrPresent
    private LocalDateTime startDate;

    @PastOrPresent
    private LocalDateTime endDate;
}
