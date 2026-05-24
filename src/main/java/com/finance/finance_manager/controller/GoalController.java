package com.finance.finance_manager.controller;

import com.finance.finance_manager.dto.GoalRequest;
import com.finance.finance_manager.dto.UpdateGoalRequest;
import com.finance.finance_manager.entity.Goal;
import com.finance.finance_manager.entity.Transaction;
import com.finance.finance_manager.entity.TransactionType;
import com.finance.finance_manager.entity.User;
import com.finance.finance_manager.repository.GoalRepository;
import com.finance.finance_manager.repository.TransactionRepository;
import com.finance.finance_manager.config.SessionUtil;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalRepository goalRepository;
    private final TransactionRepository transactionRepository;
    private final SessionUtil sessionUtil;

    @PostMapping
    public ResponseEntity<?> createGoal(
            @Valid @RequestBody GoalRequest request,
            HttpSession session) {
        User user = sessionUtil.getLoggedInUser(session);

        LocalDate startDate = request.getStartDate() == null
                ? LocalDate.now()
                : request.getStartDate();

        if (startDate.isAfter(request.getTargetDate())) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Start date cannot be after target date"));
        }

        Goal goal = Goal.builder()
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .targetDate(request.getTargetDate())
                .startDate(
                        request.getStartDate() == null
                                ? java.time.LocalDate.now()
                                : request.getStartDate())
                .user(user)
                .build();

        goalRepository.save(goal);
        return ResponseEntity.status(201).body(buildGoalResponse(goal));
    }

    @GetMapping
    public ResponseEntity<?> getGoals(HttpSession session) {
        User user = sessionUtil.getLoggedInUser(session);
        List<Goal> goals = goalRepository.findByUser(user);

        List<Map<String, Object>> response = goals.stream().map(this::buildGoalResponse).toList();
        return ResponseEntity.ok(Map.of("goals", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGoal(@PathVariable Long id, HttpSession session) {
        User user = sessionUtil.getLoggedInUser(session);
        Goal goal = goalRepository.findById(id).orElseThrow();

        if (!goal.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Forbidden"));
        }

        return ResponseEntity.ok(buildGoalResponse(goal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGoal(
            @PathVariable Long id,
            @RequestBody UpdateGoalRequest request,
            HttpSession session) {
        User user = sessionUtil.getLoggedInUser(session);
        Goal goal = goalRepository.findById(id).orElseThrow();

        if (!goal.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Forbidden"));
        }

        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }

        if (request.getTargetDate() != null) {
            goal.setTargetDate(request.getTargetDate());
        }
        goalRepository.save(goal);

        return ResponseEntity.ok(buildGoalResponse(goal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@PathVariable Long id, HttpSession session) {
        User user = sessionUtil.getLoggedInUser(session);
        Goal goal = goalRepository.findById(id).orElseThrow();

        if (!goal.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Forbidden"));
        }

        goalRepository.delete(goal);
        return ResponseEntity.ok(Map.of("message", "Goal deleted successfully"));
    }

    private Map<String, Object> buildGoalResponse(Goal goal) {
        List<Transaction> transactions = transactionRepository.findByUserOrderByDateDesc(goal.getUser());

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            if (transaction.getDate().isBefore(goal.getStartDate()))
                continue;
            if (transaction.getCategory().getType() == TransactionType.INCOME) {
                income = income.add(transaction.getAmount());
            } else {
                expense = expense.add(transaction.getAmount());
            }
        }

        BigDecimal progress = income.subtract(expense);
        if (progress.compareTo(BigDecimal.ZERO) < 0) {
            progress = BigDecimal.ZERO;
        }
        BigDecimal remaining = goal.getTargetAmount().subtract(progress);
        double percentage = progress.doubleValue() * 100
                / goal.getTargetAmount().doubleValue();

        percentage = Math.round(percentage * 100.0) / 100.0;

        return Map.of(
                "id", goal.getId(),
                "goalName", goal.getGoalName(),
                "targetAmount", goal.getTargetAmount(),
                "targetDate", goal.getTargetDate(),
                "startDate", goal.getStartDate(),
                "currentProgress", progress,
                "progressPercentage", percentage,
                "remainingAmount", remaining);
    }
}