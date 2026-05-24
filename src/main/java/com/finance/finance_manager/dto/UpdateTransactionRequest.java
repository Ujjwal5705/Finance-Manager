package com.finance.finance_manager.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateTransactionRequest {

    private BigDecimal amount;

    private String description;
}