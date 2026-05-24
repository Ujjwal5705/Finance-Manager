package com.finance.finance_manager.config;

import com.finance.finance_manager.entity.User;
import com.finance.finance_manager.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionUtil {

    private final UserRepository userRepository;

    public User getLoggedInUser(HttpSession session) {

        Object username =
                session.getAttribute("username");

        if (username == null) {
            throw new RuntimeException("Unauthorized");
        }

        return userRepository
                .findByUsername(username.toString())
                .orElseThrow();
    }
}