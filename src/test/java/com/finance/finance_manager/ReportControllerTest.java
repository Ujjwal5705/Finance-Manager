package com.finance.finance_manager;

import com.finance.finance_manager.config.SessionUtil;
import com.finance.finance_manager.controller.ReportController;
import com.finance.finance_manager.repository.TransactionRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReportControllerTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private SessionUtil sessionUtil;
    @Mock
    private HttpSession session;
    @InjectMocks
    private ReportController reportController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testMonthlyReportInvalidMonth() {
        ResponseEntity<?> response = reportController.monthlyReport(2024, 13, session);
        assertEquals(400, response.getStatusCode().value());
    }
}