package com.vending.repository;

import com.vending.entity.MachineProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MachineProductPriceRepository extends JpaRepository<MachineProductPrice, UUID> {

    List<MachineProductPrice> findByMachineId(UUID machineId);

    List<MachineProductPrice> findByProductId(UUID productId);

    Optional<MachineProductPrice> findByMachineIdAndProductId(UUID machineId, UUID productId);

    @Query("SELECT mpp FROM MachineProductPrice mpp " +
           "WHERE mpp.machine.id = :machineId " +
           "AND mpp.product.category = :category")
    List<MachineProductPrice> findByMachineAndProductCategory(
        @Param("machineId") UUID machineId,
        @Param("category") String category
    );

    void deleteByMachineIdAndProductId(UUID machineId, UUID productId);

    @Query("SELECT COUNT(mpp) FROM MachineProductPrice mpp WHERE mpp.machine.id = :machineId")
    long countByMachineId(@Param("machineId") UUID machineId);
}
