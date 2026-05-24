package com.finance.finance_manager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String goalName;

    private BigDecimal targetAmount;

    private LocalDate targetDate;

    private LocalDate startDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}