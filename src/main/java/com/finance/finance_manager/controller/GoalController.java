package com.finance.finance_manager.controller;

import com.finance.finance_manager.dto.GoalRequest;
import com.finance.finance_manager.dto.UpdateGoalRequest;
import com.finance.finance_manager.entity.Goal;
import com.finance.finance_manager.entity.Transaction;
import com.finance.finance_manager.entity.TransactionType;
import com.finance.finance_manager.entity.User;
import com.finance.finance_manager.repository.GoalRepository;
import com.finance.finance_manager.repository.TransactionRepository;
import com.finance.finance_manager.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @PostMapping
    public ResponseEntity<?> createGoal(
            @Valid @RequestBody GoalRequest request,
            Principal principal
    ) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();

        Goal goal = Goal.builder()
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .targetDate(request.getTargetDate())
                .startDate(request.getStartDate())
                .user(user)
                .build();

        goalRepository.save(goal);

        return ResponseEntity.status(201)
                .body(buildGoalResponse(goal));
    }

    @GetMapping
    public ResponseEntity<?> getGoals(Principal principal) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();

        List<Goal> goals = goalRepository.findByUser(user);

        List<Map<String, Object>> response =
                goals.stream()
                        .map(this::buildGoalResponse)
                        .toList();

        return ResponseEntity.ok(Map.of("goals", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGoal(
            @PathVariable Long id,
            Principal principal
    ) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();

        Goal goal = goalRepository.findById(id)
                .orElseThrow();

        if (!goal.getUser().getId().equals(user.getId())) {

            return ResponseEntity.status(403)
                    .body(Map.of("message", "Forbidden"));
        }

        return ResponseEntity.ok(buildGoalResponse(goal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGoal(
            @PathVariable Long id,
            @RequestBody UpdateGoalRequest request,
            Principal principal
    ) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();

        Goal goal = goalRepository.findById(id)
                .orElseThrow();

        if (!goal.getUser().getId().equals(user.getId())) {

            return ResponseEntity.status(403)
                    .body(Map.of("message", "Forbidden"));
        }

        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());

        goalRepository.save(goal);

        return ResponseEntity.ok(buildGoalResponse(goal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(
            @PathVariable Long id,
            Principal principal
    ) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();

        Goal goal = goalRepository.findById(id)
                .orElseThrow();

        if (!goal.getUser().getId().equals(user.getId())) {

            return ResponseEntity.status(403)
                    .body(Map.of("message", "Forbidden"));
        }

        goalRepository.delete(goal);

        return ResponseEntity.ok(
                Map.of("message", "Goal deleted successfully")
        );
    }

    private Map<String, Object> buildGoalResponse(Goal goal) {

        List<Transaction> transactions =
                transactionRepository.findByUserOrderByDateDesc(
                        goal.getUser()
                );

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {

            if (transaction.getDate()
                    .isBefore(goal.getStartDate())) {
                continue;
            }

            if (transaction.getCategory().getType()
                    == TransactionType.INCOME) {

                income = income.add(transaction.getAmount());

            } else {

                expense = expense.add(transaction.getAmount());
            }
        }

        BigDecimal progress = income.subtract(expense);

        BigDecimal remaining =
                goal.getTargetAmount().subtract(progress);

        double percentage =
                progress.doubleValue() * 100
                        / goal.getTargetAmount().doubleValue();

        return Map.of(
                "id", goal.getId(),
                "goalName", goal.getGoalName(),
                "targetAmount", goal.getTargetAmount(),
                "targetDate", goal.getTargetDate(),
                "startDate", goal.getStartDate(),
                "currentProgress", progress,
                "progressPercentage", percentage,
                "remainingAmount", remaining
        );
    }
}