package com.vending.repository;

import com.vending.entity.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {

    Optional<ProductCategory> findByName(String name);

    List<ProductCategory> findByActiveTrue();

    List<ProductCategory> findByActiveTrueOrderByDisplayOrderAsc();

    Page<ProductCategory> findByActive(Boolean active, Pageable pageable);

    Page<ProductCategory> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
