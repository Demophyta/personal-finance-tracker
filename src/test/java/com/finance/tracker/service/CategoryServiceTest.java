package com.finance.tracker.service;

import com.finance.tracker.dto.CategoryRequestDTO;
import com.finance.tracker.dto.CategoryResponseDTO;
import com.finance.tracker.model.Category;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.CategoryRepository;
import com.finance.tracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void shouldCreateCategorySuccessfully() {

        // Arrange
        String email = "test@gmail.com";

        User user = new User();
        user.setUserId(1L);
        user.setEmail(email);

        CategoryRequestDTO dto = new CategoryRequestDTO();
        dto.setName("Food");

        Category savedCategory = new Category();
        savedCategory.setCategoryId(1L);
        savedCategory.setName("Food");
        savedCategory.setUser(user);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(categoryRepository.existsByUserAndNameIgnoreCase(user, "Food"))
                .thenReturn(false);

        when(categoryRepository.save(any(Category.class)))
                .thenReturn(savedCategory);

        // Act
        CategoryResponseDTO result =
                categoryService.createCategory(dto, email);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getCategoryId());
        assertEquals("Food", result.getName());
        assertEquals(1L, result.getUserId());

        verify(userRepository).findByEmail(email);
        verify(categoryRepository)
                .existsByUserAndNameIgnoreCase(user, "Food");
        verify(categoryRepository).save(any(Category.class));
    }
}