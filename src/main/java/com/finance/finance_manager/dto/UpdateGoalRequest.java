package com.finance.finance_manager.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateGoalRequest {

    private BigDecimal targetAmount;

    private LocalDate targetDate;
}