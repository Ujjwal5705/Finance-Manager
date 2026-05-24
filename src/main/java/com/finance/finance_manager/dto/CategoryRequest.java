package com.finance.finance_manager.dto;

import com.finance.finance_manager.entity.TransactionType;
import lombok.Data;

@Data
public class CategoryRequest {

    private String name;

    private TransactionType type;
}