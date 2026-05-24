package com.finance.finance_manager.repository;

import com.finance.finance_manager.entity.Category;
import com.finance.finance_manager.entity.TransactionType;
import com.finance.finance_manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserOrIsCustomFalse(User user);

    Optional<Category> findByNameAndUser(String name, User user);

    boolean existsByNameAndUser(String name, User user);

    List<Category> findByType(TransactionType type);
}