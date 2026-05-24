package com.finance.finance_manager.config;

import com.finance.finance_manager.entity.Category;
import com.finance.finance_manager.entity.TransactionType;
import com.finance.finance_manager.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final CategoryRepository categoryRepository;

    @PostConstruct
    public void init() {

        createDefault("Salary", TransactionType.INCOME);

        createDefault("Food", TransactionType.EXPENSE);
        createDefault("Rent", TransactionType.EXPENSE);
        createDefault("Transportation", TransactionType.EXPENSE);
        createDefault("Entertainment", TransactionType.EXPENSE);
        createDefault("Healthcare", TransactionType.EXPENSE);
        createDefault("Utilities", TransactionType.EXPENSE);
    }

    private void createDefault(
            String name,
            TransactionType type) {

        boolean exists = categoryRepository.findAll()
                .stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(name));

        if (!exists) {

            Category category = Category.builder()
                    .name(name)
                    .type(type)
                    .isCustom(false)
                    .build();

            categoryRepository.save(category);
        }
    }
}