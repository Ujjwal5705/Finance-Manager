package com.finance.finance_manager.controller;

import com.finance.finance_manager.dto.TransactionRequest;
import com.finance.finance_manager.entity.Category;
import com.finance.finance_manager.entity.Transaction;
import com.finance.finance_manager.entity.User;
import com.finance.finance_manager.repository.CategoryRepository;
import com.finance.finance_manager.repository.TransactionRepository;
import com.finance.finance_manager.config.SessionUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final SessionUtil sessionUtil;

    @PostMapping
    public ResponseEntity<?> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            HttpSession session) {

        User user = sessionUtil.getLoggedInUser(session);

        Category category = categoryRepository.findByNameAndUser(request.getCategory(), user)
                .orElse(categoryRepository.findByUserOrIsCustomFalse(user).stream()
                        .filter(c -> c.getName().equalsIgnoreCase(request.getCategory()))
                        .findFirst()
                        .orElseThrow());

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .date(request.getDate())
                .description(request.getDescription())
                .category(category)
                .user(user)
                .build();

        transaction = transactionRepository.save(transaction);

        return ResponseEntity.status(201).body(Map.of(
                "id", transaction.getId(),
                "amount", transaction.getAmount().setScale(2),
                "date", transaction.getDate(),
                "category", category.getName(),
                "description", transaction.getDescription(),
                "type", category.getType()));
    }

    @GetMapping
    public ResponseEntity<?> getTransactions(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String category,
            HttpSession session) {
        User user = sessionUtil.getLoggedInUser(session);

        List<Transaction> transactions = (startDate != null && endDate != null)
                ? transactionRepository.findByUserAndDateBetweenOrderByDateDesc(user, startDate,
                        endDate)
                : transactionRepository.findByUserOrderByDateDesc(user);

        if (category != null) {
            transactions = transactions.stream()
                    .filter(t -> t.getCategory()
                            .getName()
                            .equalsIgnoreCase(category))
                    .toList();
        }

        List<Map<String, Object>> response = transactions.stream().map(t -> {
            Map<String, Object> map = new HashMap<>();
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
            @RequestBody Map<String, Object> request,
            HttpSession session) {

        User user = sessionUtil.getLoggedInUser(session);

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow();

        if (!transaction.getUser().getId().equals(user.getId())) {

            return ResponseEntity.status(403)
                    .body(Map.of("message", "Forbidden"));
        }

        if (request.containsKey("amount")) {

            transaction.setAmount(
                    new java.math.BigDecimal(
                            request.get("amount").toString()));
        }

        if (request.containsKey("description")) {

            transaction.setDescription(
                    request.get("description").toString());
        }

        transactionRepository.save(transaction);

        return ResponseEntity.ok(Map.of(
                "id", transaction.getId(),
                "amount", transaction.getAmount().setScale(2),
                "date", transaction.getDate(),
                "category", transaction.getCategory().getName(),
                "description", transaction.getDescription(),
                "type", transaction.getCategory().getType()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id, HttpSession session) {
        User user = sessionUtil.getLoggedInUser(session);

        Transaction transaction = transactionRepository.findById(id).orElseThrow();
        if (!transaction.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Forbidden"));
        }

        transactionRepository.delete(transaction);
        return ResponseEntity.ok(Map.of("message", "Transaction deleted successfully"));
    }
}