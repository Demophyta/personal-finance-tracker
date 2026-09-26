package com.finance.tracker.repository;

import com.finance.tracker.model.Category;
import com.finance.tracker.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUser(User user, Sort sort);
    boolean existsByUserAndNameIgnoreCase(User user, String name);
    List<Category> findByUserAndNameContainingIgnoreCase(User user, String name, Sort sort);
}
