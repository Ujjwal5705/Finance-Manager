package com.finance.finance_manager.controller;

import com.finance.finance_manager.dto.TransactionRequest;
import com.finance.finance_manager.dto.UpdateTransactionRequest;
import com.finance.finance_manager.entity.Category;
import com.finance.finance_manager.entity.Transaction;
import com.finance.finance_manager.entity.User;
import com.finance.finance_manager.repository.CategoryRepository;
import com.finance.finance_manager.repository.TransactionRepository;
import com.finance.finance_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            Principal principal
    ) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();

        Category category = categoryRepository
                .findByNameAndUser(request.getCategory(), user)
                .orElse(null);

        if (category == null) {

            List<Category> defaultCategories =
                    categoryRepository.findByUserOrIsCustomFalse(user);

            category = defaultCategories.stream()
                    .filter(c -> c.getName()
                            .equalsIgnoreCase(request.getCategory()))
                    .findFirst()
                    .orElseThrow();
        }

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .date(request.getDate())
                .description(request.getDescription())
                .category(category)
                .user(user)
                .build();

        transactionRepository.save(transaction);

        return ResponseEntity.status(201).body(Map.of(
                "id", transaction.getId(),
                "amount", transaction.getAmount(),
                "date", transaction.getDate(),
                "category", category.getName(),
                "description", transaction.getDescription(),
                "type", category.getType()
        ));
    }

    @GetMapping
    public ResponseEntity<?> getTransactions(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Principal principal
    ) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();

        List<Transaction> transactions;

        if (startDate != null && endDate != null) {

            transactions =
                    transactionRepository
                            .findByUserAndDateBetweenOrderByDateDesc(
                                    user,
                                    startDate,
                                    endDate
                            );

        } else {

            transactions =
                    transactionRepository.findByUserOrderByDateDesc(user);
        }

        List<Map<String, Object>> response = transactions.stream().map(t -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", t.getId());
                map.put("amount", t.getAmount());
                map.put("date", t.getDate());
                map.put("category", t.getCategory().getName());
                map.put("description", t.getDescription() == null ? "" : t.getDescription());
                map.put("type", t.getCategory().getType());
                return map;
            }).toList();

        return ResponseEntity.ok(Map.of("transactions", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransaction(
            @PathVariable Long id,
            @RequestBody UpdateTransactionRequest request,
            Principal principal
    ) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();

        Transaction transaction =
                transactionRepository.findById(id)
                        .orElseThrow();

        if (!transaction.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Forbidden"));
        }

        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());

        transactionRepository.save(transaction);

        return ResponseEntity.ok(Map.of(
                "id", transaction.getId(),
                "amount", transaction.getAmount(),
                "date", transaction.getDate(),
                "category", transaction.getCategory().getName(),
                "description", transaction.getDescription(),
                "type", transaction.getCategory().getType()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(
            @PathVariable Long id,
            Principal principal
    ) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();

        Transaction transaction =
                transactionRepository.findById(id)
                        .orElseThrow();

        if (!transaction.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Forbidden"));
        }

        transactionRepository.delete(transaction);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Transaction deleted successfully"
                )
        );
    }
}