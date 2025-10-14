package com.vending.controller;

import com.vending.entity.Product;
import com.vending.exception.DuplicateResourceException;
import com.vending.exception.ResourceNotFoundException;
import com.vending.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<?> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean lowStock
    ) {
        // If no pagination parameters provided, return simple list for backward compatibility
        if (page == 0 && size == 10 && search == null && category == null && active == null && lowStock == null) {
            return ResponseEntity.ok(productRepository.findAll());
        }

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                    Sort.by(sortBy).ascending() :
                    Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productsPage;

        // Apply filters
        if (search != null && !search.trim().isEmpty()) {
            productsPage = productRepository.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                search, search, pageable);
        } else if (category != null && !category.trim().isEmpty()) {
            if (active != null) {
                productsPage = productRepository.findByCategoryAndActive(category, active, pageable);
            } else {
                productsPage = productRepository.findByCategory(category, pageable);
            }
        } else if (active != null) {
            productsPage = productRepository.findByActive(active, pageable);
        } else {
            productsPage = productRepository.findAll(pageable);
        }

        // Create response with pagination metadata
        Map<String, Object> response = new HashMap<>();
        response.put("products", productsPage.getContent());
        response.put("currentPage", productsPage.getNumber());
        response.put("totalItems", productsPage.getTotalElements());
        response.put("totalPages", productsPage.getTotalPages());
        response.put("pageSize", productsPage.getSize());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return ResponseEntity.ok(product);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Product>> getActiveProducts() {
        return ResponseEntity.ok(productRepository.findByActiveTrue());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productRepository.findByCategoryAndActiveTrue(category));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        // Check for duplicate name
        if (product.getName() != null && productRepository.findByName(product.getName()).isPresent()) {
            throw new DuplicateResourceException("Product", "name", product.getName());
        }
        // Check for duplicate barcode
        if (product.getBarcode() != null && !product.getBarcode().isEmpty()
                && productRepository.findByBarcode(product.getBarcode()).isPresent()) {
            throw new DuplicateResourceException("Product", "barcode", product.getBarcode());
        }
        // Check for duplicate SKU
        if (product.getSku() != null && !product.getSku().isEmpty()
                && productRepository.findBySku(product.getSku()).isPresent()) {
            throw new DuplicateResourceException("Product", "sku", product.getSku());
        }

        Product saved = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable UUID id, @Valid @RequestBody Product productUpdate) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // Check for duplicate name (excluding current product)
        if (productUpdate.getName() != null && !productUpdate.getName().equals(product.getName())) {
            productRepository.findByName(productUpdate.getName()).ifPresent(p -> {
                throw new DuplicateResourceException("Product", "name", productUpdate.getName());
            });
        }

        // Check for duplicate barcode (excluding current product)
        if (productUpdate.getBarcode() != null && !productUpdate.getBarcode().isEmpty()
                && !productUpdate.getBarcode().equals(product.getBarcode())) {
            productRepository.findByBarcode(productUpdate.getBarcode()).ifPresent(p -> {
                throw new DuplicateResourceException("Product", "barcode", productUpdate.getBarcode());
            });
        }

        // Check for duplicate SKU (excluding current product)
        if (productUpdate.getSku() != null && !productUpdate.getSku().isEmpty()
                && !productUpdate.getSku().equals(product.getSku())) {
            productRepository.findBySku(productUpdate.getSku()).ifPresent(p -> {
                throw new DuplicateResourceException("Product", "sku", productUpdate.getSku());
            });
        }

        if (productUpdate.getName() != null) product.setName(productUpdate.getName());
        if (productUpdate.getCategory() != null) product.setCategory(productUpdate.getCategory());
        if (productUpdate.getBasePrice() != null) product.setBasePrice(productUpdate.getBasePrice());
        if (productUpdate.getCurrentStock() != null) product.setCurrentStock(productUpdate.getCurrentStock());
        product.setActive(productUpdate.isActive());
        product.setHstExempt(productUpdate.isHstExempt());

        return ResponseEntity.ok(productRepository.save(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
