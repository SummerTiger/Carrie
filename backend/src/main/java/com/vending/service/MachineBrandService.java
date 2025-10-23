package com.vending.service;

import com.vending.entity.MachineBrand;
import com.vending.exception.DuplicateResourceException;
import com.vending.exception.ResourceNotFoundException;
import com.vending.repository.MachineBrandRepository;
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
public class MachineBrandService {

    private final MachineBrandRepository brandRepository;

    public List<MachineBrand> getAllBrands() {
        return brandRepository.findAll();
    }

    public List<MachineBrand> getAllActiveBrands() {
        return brandRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }

    public Page<MachineBrand> getAllBrands(Pageable pageable) {
        return brandRepository.findAll(pageable);
    }

    public Page<MachineBrand> searchBrands(String name, Pageable pageable) {
        return brandRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    public MachineBrand getBrandById(UUID id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MachineBrand", "id", id));
    }

    public MachineBrand createBrand(MachineBrand brand) {
        if (brandRepository.findByName(brand.getName()).isPresent()) {
            throw new DuplicateResourceException("MachineBrand", "name", brand.getName());
        }
        return brandRepository.save(brand);
    }

    public MachineBrand updateBrand(UUID id, MachineBrand brandUpdate) {
        MachineBrand brand = getBrandById(id);

        if (brandUpdate.getName() != null && !brandUpdate.getName().equals(brand.getName())) {
            brandRepository.findByName(brandUpdate.getName()).ifPresent(b -> {
                throw new DuplicateResourceException("MachineBrand", "name", brandUpdate.getName());
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
        if (brandUpdate.getSupportPhone() != null) {
            brand.setSupportPhone(brandUpdate.getSupportPhone());
        }
        if (brandUpdate.getSupportEmail() != null) {
            brand.setSupportEmail(brandUpdate.getSupportEmail());
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
            throw new ResourceNotFoundException("MachineBrand", "id", id);
        }
        brandRepository.deleteById(id);
    }
}
