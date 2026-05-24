package com.finance.finance_manager.controller;

import com.finance.finance_manager.entity.Transaction;
import com.finance.finance_manager.entity.TransactionType;
import com.finance.finance_manager.entity.User;
import com.finance.finance_manager.repository.TransactionRepository;
import com.finance.finance_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<?> monthlyReport(
            @PathVariable int year,
            @PathVariable int month,
            Principal principal
    ) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();

        List<Transaction> transactions =
                transactionRepository.findByUserOrderByDateDesc(user);

        Map<String, BigDecimal> incomeMap = new HashMap<>();
        Map<String, BigDecimal> expenseMap = new HashMap<>();

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;

        for (Transaction t : transactions) {

            LocalDate date = t.getDate();

            if (date.getYear() == year &&
                    date.getMonthValue() == month) {

                String category = t.getCategory().getName();

                if (t.getCategory().getType()
                        == TransactionType.INCOME) {

                    incomeMap.put(
                            category,
                            incomeMap.getOrDefault(
                                    category,
                                    BigDecimal.ZERO
                            ).add(t.getAmount())
                    );

                    income = income.add(t.getAmount());

                } else {

                    expenseMap.put(
                            category,
                            expenseMap.getOrDefault(
                                    category,
                                    BigDecimal.ZERO
                            ).add(t.getAmount())
                    );

                    expense = expense.add(t.getAmount());
                }
            }
        }

        return ResponseEntity.ok(Map.of(
                "month", month,
                "year", year,
                "totalIncome", incomeMap,
                "totalExpenses", expenseMap,
                "netSavings", income.subtract(expense)
        ));
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<?> yearlyReport(
            @PathVariable int year,
            Principal principal
    ) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();

        List<Transaction> transactions =
                transactionRepository.findByUserOrderByDateDesc(user);

        Map<String, BigDecimal> incomeMap = new HashMap<>();
        Map<String, BigDecimal> expenseMap = new HashMap<>();

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;

        for (Transaction t : transactions) {

            if (t.getDate().getYear() == year) {

                String category = t.getCategory().getName();

                if (t.getCategory().getType()
                        == TransactionType.INCOME) {

                    incomeMap.put(
                            category,
                            incomeMap.getOrDefault(
                                    category,
                                    BigDecimal.ZERO
                            ).add(t.getAmount())
                    );

                    income = income.add(t.getAmount());

                } else {

                    expenseMap.put(
                            category,
                            expenseMap.getOrDefault(
                                    category,
                                    BigDecimal.ZERO
                            ).add(t.getAmount())
                    );

                    expense = expense.add(t.getAmount());
                }
            }
        }

        return ResponseEntity.ok(Map.of(
                "year", year,
                "totalIncome", incomeMap,
                "totalExpenses", expenseMap,
                "netSavings", income.subtract(expense)
        ));
    }
}