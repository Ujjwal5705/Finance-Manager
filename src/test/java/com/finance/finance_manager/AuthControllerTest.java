package com.finance.finance_manager;

import com.finance.finance_manager.controller.AuthController;
import com.finance.finance_manager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.MockitoAnnotations.openMocks;

public class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    public AuthControllerTest() {
        openMocks(this);
    }

    @Test
    void contextLoads() {
        assertNotNull(authController);
    }
}