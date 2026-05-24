package com.finance.finance_manager.repository;

import com.finance.finance_manager.entity.Goal;
import com.finance.finance_manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByUser(User user);
}