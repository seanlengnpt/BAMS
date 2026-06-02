package com.shopee.banking.bams.api.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ExportCSVByDatesRequest extends BaseAppRequest{
    @PastOrPresent
    @NotNull
    private LocalDateTime startDate;

    @PastOrPresent
    @NotNull
    private LocalDateTime endDate;
}
