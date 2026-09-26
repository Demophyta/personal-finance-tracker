package com.finance.tracker.controller;

import com.finance.tracker.dto.CategoryRequestDTO;
import com.finance.tracker.dto.CategoryResponseDTO;
import com.finance.tracker.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @RequestBody CategoryRequestDTO categoryDTO,
            Authentication authentication
    ) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                categoryService.createCategory(categoryDTO, email)
        );
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getCategories(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                categoryService.getUserCategories(email)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequestDTO updatedCategory,
            Authentication authentication
    ) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                categoryService.updateCategory(id, updatedCategory.getName(), email)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();

        categoryService.deleteCategory(id, email);

        return ResponseEntity.ok("Category deleted");
    }
}