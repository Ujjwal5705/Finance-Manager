package com.finance.finance_manager;

import com.finance.finance_manager.config.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class SessionUtilTest {

    @InjectMocks
    private SessionUtil sessionUtil;
    @Mock
    private HttpSession session;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetLoggedInUserThrowsExceptionWhenNull() {
        when(session.getAttribute("user")).thenReturn(null);
        assertThrows(RuntimeException.class, () -> sessionUtil.getLoggedInUser(session));
    }
}