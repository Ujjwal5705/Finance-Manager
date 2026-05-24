package com.finance.finance_manager.controller;

import com.finance.finance_manager.entity.Transaction;
import com.finance.finance_manager.dto.CategoryRequest;
import com.finance.finance_manager.entity.Category;
import com.finance.finance_manager.entity.User;
import com.finance.finance_manager.repository.CategoryRepository;
import com.finance.finance_manager.repository.TransactionRepository;
import com.finance.finance_manager.config.SessionUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final SessionUtil sessionUtil;

    @GetMapping
    public ResponseEntity<?> getCategories(HttpSession session) {
        User user = sessionUtil.getLoggedInUser(session);

        List<Category> categories = categoryRepository.findByUserOrIsCustomFalse(user);

        return ResponseEntity.ok(Map.of("categories", categories));
    }

    @PostMapping
    public ResponseEntity<?> createCategory(
            @Valid @RequestBody CategoryRequest request,
            HttpSession session) {
        User user = sessionUtil.getLoggedInUser(session);

        if (categoryRepository.existsByNameAndUser(request.getName(), user)) {
            return ResponseEntity.status(409)
                    .body(Map.of("message", "Category already exists"));
        }

        Category category = Category.builder()
                .name(request.getName())
                .type(request.getType())
                .isCustom(true)
                .user(user)
                .build();

        categoryRepository.save(category);

        return ResponseEntity.status(201).body(category);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<?> deleteCategory(
            @PathVariable String name,
            HttpSession session) {
        User user = sessionUtil.getLoggedInUser(session);

        Category category = categoryRepository
                .findByNameAndUser(name, user)
                .orElseThrow();

        List<Transaction> transactions = transactionRepository.findAll()
                .stream()
                .filter(t -> t.getCategory().getId().equals(category.getId()))
                .toList();

        if (!transactions.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Category is in use"));
        }

        categoryRepository.delete(category);

        return ResponseEntity.ok(
                Map.of("message", "Category deleted successfully"));
    }
}