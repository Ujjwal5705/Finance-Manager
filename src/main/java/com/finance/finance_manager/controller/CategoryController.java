package com.finance.finance_manager.controller;

import com.finance.finance_manager.dto.CategoryRequest;
import com.finance.finance_manager.entity.Category;
import com.finance.finance_manager.entity.User;
import com.finance.finance_manager.repository.CategoryRepository;
import com.finance.finance_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getCategories(Principal principal) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();

        List<Category> categories =
                categoryRepository.findByUserOrIsCustomFalse(user);

        return ResponseEntity.ok(Map.of("categories", categories));
    }

    @PostMapping
    public ResponseEntity<?> createCategory(
            @Valid @RequestBody CategoryRequest request,
            Principal principal
    ) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();

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
            Principal principal
    ) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();

        Category category = categoryRepository
                .findByNameAndUser(name, user)
                .orElseThrow();

        categoryRepository.delete(category);

        return ResponseEntity.ok(
                Map.of("message", "Category deleted successfully")
        );
    }
}