package com.vending.service;

import com.vending.entity.ProductBrand;
import com.vending.exception.DuplicateResourceException;
import com.vending.exception.ResourceNotFoundException;
import com.vending.repository.ProductBrandRepository;
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
public class ProductBrandService {

    private final ProductBrandRepository brandRepository;

    public List<ProductBrand> getAllBrands() {
        return brandRepository.findAll();
    }

    public List<ProductBrand> getAllActiveBrands() {
        return brandRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }

    public Page<ProductBrand> getAllBrands(Pageable pageable) {
        return brandRepository.findAll(pageable);
    }

    public Page<ProductBrand> searchBrands(String name, Pageable pageable) {
        return brandRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    public ProductBrand getBrandById(UUID id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBrand", "id", id));
    }

    public ProductBrand createBrand(ProductBrand brand) {
        // Check for duplicate name
        if (brandRepository.findByName(brand.getName()).isPresent()) {
            throw new DuplicateResourceException("ProductBrand", "name", brand.getName());
        }
        return brandRepository.save(brand);
    }

    public ProductBrand updateBrand(UUID id, ProductBrand brandUpdate) {
        ProductBrand brand = getBrandById(id);

        // Check for duplicate name (excluding current brand)
        if (brandUpdate.getName() != null && !brandUpdate.getName().equals(brand.getName())) {
            brandRepository.findByName(brandUpdate.getName()).ifPresent(b -> {
                throw new DuplicateResourceException("ProductBrand", "name", brandUpdate.getName());
            });
            brand.setName(brandUpdate.getName());
        }

        if (brandUpdate.getDescription() != null) {
            brand.setDescription(brandUpdate.getDescription());
        }
        if (brandUpdate.getLogoUrl() != null) {
            brand.setLogoUrl(brandUpdate.getLogoUrl());
        }
        if (brandUpdate.getWebsite() != null) {
            brand.setWebsite(brandUpdate.getWebsite());
        }
        if (brandUpdate.getActive() != null) {
            brand.setActive(brandUpdate.getActive());
        }
        if (brandUpdate.getDisplayOrder() != null) {
            brand.setDisplayOrder(brandUpdate.getDisplayOrder());
        }

        return brandRepository.save(brand);
    }

    public void deleteBrand(UUID id) {
        if (!brandRepository.existsById(id)) {
            throw new ResourceNotFoundException("ProductBrand", "id", id);
        }
        brandRepository.deleteById(id);
    }
}
