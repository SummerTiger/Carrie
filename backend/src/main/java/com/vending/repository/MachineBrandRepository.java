package com.vending.repository;

import com.vending.entity.MachineBrand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MachineBrandRepository extends JpaRepository<MachineBrand, UUID> {

    Optional<MachineBrand> findByName(String name);

    List<MachineBrand> findByActiveTrue();

    List<MachineBrand> findByActiveTrueOrderByDisplayOrderAsc();

    Page<MachineBrand> findByActive(Boolean active, Pageable pageable);

    Page<MachineBrand> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
