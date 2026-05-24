package com.finance.finance_manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.finance.finance_manager.dto.TransactionRequest;
import com.finance.finance_manager.entity.Category;
import com.finance.finance_manager.entity.Transaction;
import com.finance.finance_manager.entity.TransactionType;
import com.finance.finance_manager.entity.User;
import com.finance.finance_manager.repository.CategoryRepository;
import com.finance.finance_manager.repository.TransactionRepository;
import com.finance.finance_manager.config.SessionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SessionUtil sessionUtil;

    @InjectMocks
    private TransactionController transactionController;

    private User testUser;
    private Category expenseCategory;
    private Category incomeCategory;
    private Transaction testTransaction;

    @ControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(NoSuchElementException.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        void handleNotFound() {
        }
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(transactionController)
                .setControllerAdvice(new TestExceptionHandler())
                .setMessageConverters(converter)
                .build();

        testUser = new User();
        testUser.setId(1L);

        expenseCategory = new Category();
        expenseCategory.setId(1L);
        expenseCategory.setName("Food");
        expenseCategory.setType(TransactionType.EXPENSE);
        expenseCategory.setUser(testUser);

        incomeCategory = new Category();
        incomeCategory.setId(2L);
        incomeCategory.setName("Salary");
        incomeCategory.setType(TransactionType.INCOME);
        incomeCategory.setUser(testUser);

        testTransaction = Transaction.builder()
                .id(10L)
                .amount(new BigDecimal("42.50"))
                .date(LocalDate.of(2025, 5, 24))
                .description("Groceries")
                .category(expenseCategory)
                .user(testUser)
                .build();
    }

    @Test
    void createTransaction_Success() throws Exception {
        when(sessionUtil.getLoggedInUser(any())).thenReturn(testUser);

        when(categoryRepository.findByNameAndUser(anyString(), any(User.class)))
                .thenReturn(Optional.of(expenseCategory));

        when(categoryRepository.findByUserOrIsCustomFalse(any(User.class)))
                .thenReturn(List.of(expenseCategory, incomeCategory));

        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("42.50"));
        request.setDate(LocalDate.of(2025, 5, 24));
        request.setDescription("Groceries");
        request.setCategory("Food");

        Transaction savedTransaction = Transaction.builder()
                .id(1L)
                .amount(request.getAmount())
                .date(request.getDate())
                .description(request.getDescription())
                .category(expenseCategory)
                .user(testUser)
                .build();
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(42.50))
                .andExpect(jsonPath("$.category").value("Food"))
                .andExpect(jsonPath("$.type").value("EXPENSE"));

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void createTransaction_CategoryNotFound_ThrowsException() throws Exception {
        when(sessionUtil.getLoggedInUser(any())).thenReturn(testUser);
        when(categoryRepository.findByNameAndUser(anyString(), any(User.class)))
                .thenReturn(Optional.empty());
        when(categoryRepository.findByUserOrIsCustomFalse(any(User.class)))
                .thenReturn(List.of(expenseCategory, incomeCategory));

        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("10.00"));
        request.setDate(LocalDate.now());
        request.setCategory("Unknown");

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getTransactions_NoFilters_ReturnsAllUserTransactions() throws Exception {
        when(sessionUtil.getLoggedInUser(any())).thenReturn(testUser);
        when(transactionRepository.findByUserOrderByDateDesc(any(User.class)))
                .thenReturn(List.of(testTransaction));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions.length()").value(1))
                .andExpect(jsonPath("$.transactions[0].id").value(10))
                .andExpect(jsonPath("$.transactions[0].amount").value(42.50));
    }

    @Test
    void getTransactions_WithDateRange_ReturnsFiltered() throws Exception {
        when(sessionUtil.getLoggedInUser(any())).thenReturn(testUser);
        LocalDate start = LocalDate.of(2025, 5, 1);
        LocalDate end = LocalDate.of(2025, 5, 31);
        when(transactionRepository.findByUserAndDateBetweenOrderByDateDesc(any(User.class), eq(start), eq(end)))
                .thenReturn(List.of(testTransaction));

        mockMvc.perform(get("/api/transactions")
                .param("startDate", start.toString())
                .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions.length()").value(1));
    }

    @Test
    void getTransactions_WithCategoryFilter_ReturnsMatching() throws Exception {
        when(sessionUtil.getLoggedInUser(any())).thenReturn(testUser);
        when(transactionRepository.findByUserOrderByDateDesc(any(User.class)))
                .thenReturn(List.of(testTransaction));

        mockMvc.perform(get("/api/transactions")
                .param("category", "Food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions.length()").value(1))
                .andExpect(jsonPath("$.transactions[0].category").value("Food"));
    }

    @Test
    void getTransactions_WithNonMatchingCategory_ReturnsEmptyList() throws Exception {
        when(sessionUtil.getLoggedInUser(any())).thenReturn(testUser);
        when(transactionRepository.findByUserOrderByDateDesc(any(User.class)))
                .thenReturn(List.of(testTransaction));

        mockMvc.perform(get("/api/transactions")
                .param("category", "Rent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions.length()").value(0));
    }

    @Test
    void updateTransaction_Success() throws Exception {
        when(sessionUtil.getLoggedInUser(any())).thenReturn(testUser);
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(testTransaction));

        String updatePayload = "{\"amount\": 99.99, \"description\": \"Updated description\"}";

        mockMvc.perform(put("/api/transactions/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(99.99))
                .andExpect(jsonPath("$.description").value("Updated description"));

        verify(transactionRepository).save(testTransaction);
    }

    @Test
    void updateTransaction_UserMismatch_ReturnsForbidden() throws Exception {
        User otherUser = new User();
        otherUser.setId(999L);
        when(sessionUtil.getLoggedInUser(any())).thenReturn(otherUser);
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(testTransaction));

        mockMvc.perform(put("/api/transactions/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 50.00}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Forbidden"));
    }

    @Test
    void updateTransaction_NotFound_ThrowsException() throws Exception {
        when(sessionUtil.getLoggedInUser(any())).thenReturn(testUser);
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/transactions/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 10.00}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteTransaction_Success() throws Exception {
        when(sessionUtil.getLoggedInUser(any())).thenReturn(testUser);
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(testTransaction));

        mockMvc.perform(delete("/api/transactions/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));

        verify(transactionRepository).delete(testTransaction);
    }

    @Test
    void deleteTransaction_UserMismatch_ReturnsForbidden() throws Exception {
        User otherUser = new User();
        otherUser.setId(888L);
        when(sessionUtil.getLoggedInUser(any())).thenReturn(otherUser);
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(testTransaction));

        mockMvc.perform(delete("/api/transactions/10"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Forbidden"));
    }
}