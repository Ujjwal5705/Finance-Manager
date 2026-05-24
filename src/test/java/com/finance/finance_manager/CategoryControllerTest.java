package com.finance.finance_manager;

import com.finance.finance_manager.config.SessionUtil;
import com.finance.finance_manager.controller.CategoryController;
import com.finance.finance_manager.entity.Category;
import com.finance.finance_manager.entity.Transaction;
import com.finance.finance_manager.entity.User;
import com.finance.finance_manager.repository.CategoryRepository;
import com.finance.finance_manager.repository.TransactionRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class CategoryControllerTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private SessionUtil sessionUtil;
    @Mock
    private HttpSession session;
    @InjectMocks
    private CategoryController categoryController;

    private User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        user = User.builder().id(1L).build();
    }

    @Test
    void testDeleteCategoryInUse() {
        Category cat = Category.builder().id(1L).build();

        Transaction transaction = Transaction.builder()
                .category(cat)
                .build();

        when(sessionUtil.getLoggedInUser(session)).thenReturn(user);
        when(categoryRepository.findByNameAndUser("Test", user)).thenReturn(Optional.of(cat));
        when(transactionRepository.findAll()).thenReturn(List.of(transaction));

        ResponseEntity<?> response = categoryController.deleteCategory("Test", session);

        assertEquals(400, response.getStatusCode().value());
    }
}