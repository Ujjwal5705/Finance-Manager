package com.finance.finance_manager.repository;

import com.finance.finance_manager.entity.Transaction;
import com.finance.finance_manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserOrderByDateDesc(User user);

    List<Transaction> findByUserAndDateBetweenOrderByDateDesc(
            User user,
            LocalDate startDate,
            LocalDate endDate);
}