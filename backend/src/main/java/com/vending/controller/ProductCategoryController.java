package com.vending.controller;

import com.vending.entity.ProductCategory;
import com.vending.service.ProductCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/product-categories")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping
    public ResponseEntity<?> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search
    ) {
        // If no pagination parameters, return simple list
        if (page == 0 && size == 10 && search == null) {
            return ResponseEntity.ok(categoryService.getAllCategories());
        }

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                    Sort.by(sortBy).ascending() :
                    Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductCategory> categoriesPage;
        if (search != null && !search.trim().isEmpty()) {
            categoriesPage = categoryService.searchCategories(search, pageable);
        } else {
            categoriesPage = categoryService.getAllCategories(pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("categories", categoriesPage.getContent());
        response.put("currentPage", categoriesPage.getNumber());
        response.put("totalItems", categoriesPage.getTotalElements());
        response.put("totalPages", categoriesPage.getTotalPages());
        response.put("pageSize", categoriesPage.getSize());

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/active")
    public ResponseEntity<List<ProductCategory>> getActiveCategories() {
        return ResponseEntity.ok(categoryService.getAllActiveCategories());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductCategory> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<ProductCategory> createCategory(@Valid @RequestBody ProductCategory category) {
        ProductCategory created = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductCategory> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody ProductCategory category
    ) {
        ProductCategory updated = categoryService.updateCategory(id, category);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
