package com.vending.controller;

import com.vending.dto.ProductDto;
import com.vending.entity.Product;
import com.vending.entity.ProductBrand;
import com.vending.entity.ProductCategory;
import com.vending.exception.DuplicateResourceException;
import com.vending.exception.ResourceNotFoundException;
import com.vending.repository.ProductBrandRepository;
import com.vending.repository.ProductCategoryRepository;
import com.vending.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/products")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private ProductBrandRepository productBrandRepository;

    // Security: Read access for ADMIN and MANAGER roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
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

    // Security: Read access for ADMIN and MANAGER roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return ResponseEntity.ok(product);
    }

    // Security: Read access for ADMIN and MANAGER roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/active")
    public ResponseEntity<List<Product>> getActiveProducts() {
        return ResponseEntity.ok(productRepository.findByActiveTrue());
    }

    // Security: Read access for ADMIN and MANAGER roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, Object>>> getAllCategories() {
        List<Product> allProducts = productRepository.findAll();

        // Group products by category and create category summary
        Map<String, Map<String, Object>> categoryMap = new HashMap<>();

        for (Product product : allProducts) {
            String category = product.getCategory();
            if (category == null || category.trim().isEmpty()) {
                category = "Uncategorized";
            }

            final String categoryName = category; // Make final for lambda
            categoryMap.computeIfAbsent(categoryName, k -> {
                Map<String, Object> categoryInfo = new HashMap<>();
                categoryInfo.put("name", k);
                categoryInfo.put("active", true);
                categoryInfo.put("productCount", 0);
                categoryInfo.put("description", "Category for " + k);
                categoryInfo.put("icon", getCategoryIcon(k));
                return categoryInfo;
            });

            // Increment product count
            Map<String, Object> categoryInfo = categoryMap.get(categoryName);
            categoryInfo.put("productCount", (Integer) categoryInfo.get("productCount") + 1);
        }

        return ResponseEntity.ok(new java.util.ArrayList<>(categoryMap.values()));
    }

    private String getCategoryIcon(String category) {
        if (category == null) return "📦";
        return switch (category.toLowerCase()) {
            case "beverages", "drinks" -> "🥤";
            case "snacks" -> "🍿";
            case "candy", "chocolate" -> "🍫";
            case "chips" -> "🥔";
            case "water" -> "💧";
            case "soda", "soft drinks" -> "🥤";
            case "energy drinks" -> "⚡";
            case "juice" -> "🧃";
            case "coffee" -> "☕";
            case "tea" -> "🍵";
            case "food" -> "🍱";
            case "healthy", "organic" -> "🥗";
            default -> "📦";
        };
    }

    // Security: Read access for ADMIN and MANAGER roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productRepository.findByCategoryAndActiveTrue(category));
    }

    // Security: Write access for ADMIN and MANAGER roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductDto productDto) {
        // Check for duplicate name
        if (productDto.name() != null && productRepository.findByName(productDto.name()).isPresent()) {
            throw new DuplicateResourceException("Product", "name", productDto.name());
        }
        // Check for duplicate barcode
        if (productDto.barcode() != null && !productDto.barcode().isEmpty()
                && productRepository.findByBarcode(productDto.barcode()).isPresent()) {
            throw new DuplicateResourceException("Product", "barcode", productDto.barcode());
        }
        // Check for duplicate SKU
        if (productDto.sku() != null && !productDto.sku().isEmpty()
                && productRepository.findBySku(productDto.sku()).isPresent()) {
            throw new DuplicateResourceException("Product", "sku", productDto.sku());
        }

        // Create Product entity from DTO
        Product product = new Product();
        product.setName(productDto.name());
        product.setCategory(productDto.category());
        product.setUnitSize(productDto.unitSize());
        product.setCurrentStock(productDto.currentStock());
        product.setMinimumStock(productDto.minimumStock());
        product.setHstExempt(productDto.hstExempt());
        product.setBasePrice(productDto.basePrice());
        product.setDescription(productDto.description());
        product.setBarcode(productDto.barcode());
        product.setSku(productDto.sku());
        product.setActive(productDto.active());

        // Handle foreign key relationships
        if (productDto.categoryId() != null) {
            ProductCategory category = productCategoryRepository.findById(productDto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", productDto.categoryId()));
            product.setProductCategory(category);
        }

        if (productDto.brandId() != null) {
            ProductBrand brand = productBrandRepository.findById(productDto.brandId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductBrand", "id", productDto.brandId()));
            product.setProductBrand(brand);
        }

        Product saved = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Security: Write access for ADMIN and MANAGER roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductDto productDto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // Check for duplicate name (excluding current product)
        if (productDto.name() != null && !productDto.name().equals(product.getName())) {
            productRepository.findByName(productDto.name()).ifPresent(p -> {
                throw new DuplicateResourceException("Product", "name", productDto.name());
            });
        }

        // Check for duplicate barcode (excluding current product)
        if (productDto.barcode() != null && !productDto.barcode().isEmpty()
                && !productDto.barcode().equals(product.getBarcode())) {
            productRepository.findByBarcode(productDto.barcode()).ifPresent(p -> {
                throw new DuplicateResourceException("Product", "barcode", productDto.barcode());
            });
        }

        // Check for duplicate SKU (excluding current product)
        if (productDto.sku() != null && !productDto.sku().isEmpty()
                && !productDto.sku().equals(product.getSku())) {
            productRepository.findBySku(productDto.sku()).ifPresent(p -> {
                throw new DuplicateResourceException("Product", "sku", productDto.sku());
            });
        }

        // Update basic fields
        if (productDto.name() != null) product.setName(productDto.name());
        if (productDto.category() != null) product.setCategory(productDto.category());
        if (productDto.unitSize() != null) product.setUnitSize(productDto.unitSize());
        if (productDto.basePrice() != null) product.setBasePrice(productDto.basePrice());
        if (productDto.currentStock() != null) product.setCurrentStock(productDto.currentStock());
        if (productDto.minimumStock() != null) product.setMinimumStock(productDto.minimumStock());
        if (productDto.description() != null) product.setDescription(productDto.description());
        if (productDto.barcode() != null) product.setBarcode(productDto.barcode());
        if (productDto.sku() != null) product.setSku(productDto.sku());
        product.setActive(productDto.active());
        product.setHstExempt(productDto.hstExempt());

        // Handle foreign key relationships
        if (productDto.categoryId() != null) {
            ProductCategory category = productCategoryRepository.findById(productDto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", productDto.categoryId()));
            product.setProductCategory(category);
        }

        if (productDto.brandId() != null) {
            ProductBrand brand = productBrandRepository.findById(productDto.brandId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductBrand", "id", productDto.brandId()));
            product.setProductBrand(brand);
        }

        return ResponseEntity.ok(productRepository.save(product));
    }

    // Security: Write access for ADMIN and MANAGER roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
