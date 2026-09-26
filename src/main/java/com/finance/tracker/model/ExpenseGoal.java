package com.finance.tracker.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expense_goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long goalId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyLimit;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", unique = true, nullable = false)
    private Category category;

    @Column(name = "created_at", updatable = false)
    private LocalDate createdAt;

    @PrePersist
    public void setCreatedAt() {
        this.createdAt = LocalDate.now();
    }
}