package com.shopee.banking.bams.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CreateCustomerByCsvResult {
    private int createdCount;
    private int modifiedCount;
    private List<String> errors;
}
