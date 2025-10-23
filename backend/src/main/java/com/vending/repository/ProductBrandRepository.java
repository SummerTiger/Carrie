package com.vending.repository;

import com.vending.entity.ProductBrand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductBrandRepository extends JpaRepository<ProductBrand, UUID> {

    Optional<ProductBrand> findByName(String name);

    List<ProductBrand> findByActiveTrue();

    List<ProductBrand> findByActiveTrueOrderByDisplayOrderAsc();

    Page<ProductBrand> findByActive(Boolean active, Pageable pageable);

    Page<ProductBrand> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
