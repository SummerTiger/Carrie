package com.vending.controller;

import com.vending.entity.ProductBrand;
import com.vending.service.ProductBrandService;
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
@RequestMapping("/api/product-brands")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@RequiredArgsConstructor
public class ProductBrandController {

    private final ProductBrandService brandService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping
    public ResponseEntity<?> getAllBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search
    ) {
        // If no pagination parameters, return simple list
        if (page == 0 && size == 10 && search == null) {
            return ResponseEntity.ok(brandService.getAllBrands());
        }

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                    Sort.by(sortBy).ascending() :
                    Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductBrand> brandsPage;
        if (search != null && !search.trim().isEmpty()) {
            brandsPage = brandService.searchBrands(search, pageable);
        } else {
            brandsPage = brandService.getAllBrands(pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("brands", brandsPage.getContent());
        response.put("currentPage", brandsPage.getNumber());
        response.put("totalItems", brandsPage.getTotalElements());
        response.put("totalPages", brandsPage.getTotalPages());
        response.put("pageSize", brandsPage.getSize());

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/active")
    public ResponseEntity<List<ProductBrand>> getActiveBrands() {
        return ResponseEntity.ok(brandService.getAllActiveBrands());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductBrand> getBrandById(@PathVariable UUID id) {
        return ResponseEntity.ok(brandService.getBrandById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<ProductBrand> createBrand(@Valid @RequestBody ProductBrand brand) {
        ProductBrand created = brandService.createBrand(brand);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductBrand> updateBrand(
            @PathVariable UUID id,
            @Valid @RequestBody ProductBrand brand
    ) {
        ProductBrand updated = brandService.updateBrand(id, brand);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable UUID id) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }
}
