package com.finance.finance_manager.controller;

import com.finance.finance_manager.dto.LoginRequest;
import com.finance.finance_manager.dto.RegisterRequest;
import com.finance.finance_manager.entity.User;
import com.finance.finance_manager.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        if (userRepository.existsByUsername(request.getUsername())) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "User already exists"
                    ));
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        userRepository.save(user);

        return ResponseEntity.status(201)
                .body(Map.of(
                        "message",
                        "User registered successfully",
                        "userId",
                        user.getId()
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {

        User user = userRepository
                .findByUsername(request.getUsername())
                .orElse(null);

        if (user == null ||
                !passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                )) {

            return ResponseEntity.status(401)
                    .body(Map.of(
                            "message",
                            "Invalid credentials"
                    ));
        }

        HttpSession session =
                httpRequest.getSession(true);

        session.setAttribute(
                "userId",
                user.getId()
        );

        session.setAttribute(
                "username",
                user.getUsername()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Login successful"
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request
    ) {

        HttpSession session =
                request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Logout successful"
                )
        );
    }
}