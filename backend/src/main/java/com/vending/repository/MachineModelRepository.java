package com.vending.repository;

import com.vending.entity.MachineBrand;
import com.vending.entity.MachineModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MachineModelRepository extends JpaRepository<MachineModel, UUID> {

    Optional<MachineModel> findByMachineBrandAndName(MachineBrand brand, String name);

    List<MachineModel> findByActiveTrue();

    List<MachineModel> findByActiveTrueOrderByDisplayOrderAsc();

    List<MachineModel> findByMachineBrandAndActiveTrue(MachineBrand brand);

    Page<MachineModel> findByActive(Boolean active, Pageable pageable);

    Page<MachineModel> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<MachineModel> findByMachineBrand(MachineBrand brand, Pageable pageable);
}
