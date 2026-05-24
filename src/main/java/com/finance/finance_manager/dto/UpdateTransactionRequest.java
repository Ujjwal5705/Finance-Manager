package com.finance.finance_manager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateTransactionRequest {

    private BigDecimal amount;

    private String description;

    private LocalDate date;
}