package com.finance.finance_manager.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GoalRequest {

    private String goalName;

    private BigDecimal targetAmount;

    private LocalDate targetDate;

    private LocalDate startDate;
}