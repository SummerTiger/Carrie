package com.vending.service;

import com.vending.entity.ProductCategory;
import com.vending.exception.DuplicateResourceException;
import com.vending.exception.ResourceNotFoundException;
import com.vending.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;

    public List<ProductCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<ProductCategory> getAllActiveCategories() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }

    public Page<ProductCategory> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    public Page<ProductCategory> searchCategories(String name, Pageable pageable) {
        return categoryRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    public ProductCategory getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", id));
    }

    public ProductCategory createCategory(ProductCategory category) {
        // Check for duplicate name
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new DuplicateResourceException("ProductCategory", "name", category.getName());
        }
        return categoryRepository.save(category);
    }

    public ProductCategory updateCategory(UUID id, ProductCategory categoryUpdate) {
        ProductCategory category = getCategoryById(id);

        // Check for duplicate name (excluding current category)
        if (categoryUpdate.getName() != null && !categoryUpdate.getName().equals(category.getName())) {
            categoryRepository.findByName(categoryUpdate.getName()).ifPresent(c -> {
                throw new DuplicateResourceException("ProductCategory", "name", categoryUpdate.getName());
            });
            category.setName(categoryUpdate.getName());
        }

        if (categoryUpdate.getDescription() != null) {
            category.setDescription(categoryUpdate.getDescription());
        }
        if (categoryUpdate.getIcon() != null) {
            category.setIcon(categoryUpdate.getIcon());
        }
        if (categoryUpdate.getActive() != null) {
            category.setActive(categoryUpdate.getActive());
        }
        if (categoryUpdate.getDisplayOrder() != null) {
            category.setDisplayOrder(categoryUpdate.getDisplayOrder());
        }

        return categoryRepository.save(category);
    }

    public void deleteCategory(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("ProductCategory", "id", id);
        }
        categoryRepository.deleteById(id);
    }
}
