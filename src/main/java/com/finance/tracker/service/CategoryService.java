package com.finance.tracker.service;

import com.finance.tracker.dto.CategoryRequestDTO;
import com.finance.tracker.dto.CategoryResponseDTO;
import com.finance.tracker.exception.CategoryAlreadyExistException;
import com.finance.tracker.exception.CategoryNotFoundException;
import com.finance.tracker.exception.UserNotFoundException;
import com.finance.tracker.model.Category;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.CategoryRepository;
import com.finance.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // =====================================================
    // CREATE CATEGORY
    // =====================================================

    public CategoryResponseDTO createCategory(
            CategoryRequestDTO dto,
            String email) {

        User user = getUser(email);

        String categoryName = dto.getName().trim();

        if (categoryRepository.existsByUserAndNameIgnoreCase(user, categoryName)) {
            throw new CategoryAlreadyExistException();
        }

        Category category = Category.builder()
                .name(categoryName)
                .user(user)
                .build();

        Category saved = categoryRepository.save(category);

        return mapToResponse(saved);
    }

    // =====================================================
    // GET USER CATEGORIES
    // =====================================================

    public List<CategoryResponseDTO> getUserCategories(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        List<Category> categories = categoryRepository.findByUser(
                user,
                Sort.by("name").ascending()
        );

        return categories.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =====================================================
    // UPDATE CATEGORY
    // =====================================================

    public CategoryResponseDTO updateCategory(
            Long id,
            String newName,
            String email) {

        User user = getUser(email);

        Category category = categoryRepository.findById(id)
                .filter(c -> c.getUser().getUserId().equals(user.getUserId()))
                .orElseThrow(CategoryNotFoundException::new);

        String name = newName.trim();

        if (!category.getName().equalsIgnoreCase(name)
                && categoryRepository.existsByUserAndNameIgnoreCase(user, name)) {

            throw new CategoryAlreadyExistException();
        }

        category.setName(name);

        return mapToResponse(categoryRepository.save(category));
    }

    // =====================================================
    // DELETE CATEGORY
    // =====================================================

    public void deleteCategory(Long id, String email) {

        User user = getUser(email);

        Category category = categoryRepository.findById(id)
                .filter(c -> c.getUser().getUserId().equals(user.getUserId()))
                .orElseThrow(CategoryNotFoundException::new);

        categoryRepository.delete(category);
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private User getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    private CategoryResponseDTO mapToResponse(Category category) {

        return new CategoryResponseDTO(
                category.getCategoryId(),
                category.getName(),
                category.getUser().getUserId()
        );
    }
}