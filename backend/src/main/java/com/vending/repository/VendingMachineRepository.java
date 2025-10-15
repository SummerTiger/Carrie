package com.vending.repository;

import com.vending.entity.VendingMachine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendingMachineRepository extends JpaRepository<VendingMachine, UUID> {

    List<VendingMachine> findByActiveTrue();

    List<VendingMachine> findByBrand(String brand);

    List<VendingMachine> findByBrandAndModel(String brand, String model);

    @Query("SELECT vm FROM VendingMachine vm WHERE vm.location.city = :city AND vm.active = true")
    List<VendingMachine> findActiveByCity(@Param("city") String city);

    @Query("SELECT vm FROM VendingMachine vm WHERE vm.location.address LIKE %:address% AND vm.active = true")
    List<VendingMachine> findActiveByAddressContaining(@Param("address") String address);

    @Query("SELECT vm FROM VendingMachine vm " +
           "WHERE vm.active = true " +
           "AND :category MEMBER OF vm.allowedCategories " +
           "AND :category NOT MEMBER OF vm.forbiddenCategories")
    List<VendingMachine> findMachinesAllowingCategory(@Param("category") String category);

    @Query("SELECT vm FROM VendingMachine vm WHERE vm.hasCashlessPos = true AND vm.active = true")
    List<VendingMachine> findAllWithCashlessPos();

    @Query("SELECT COUNT(vm) FROM VendingMachine vm WHERE vm.active = true")
    long countActiveMachines();

    Optional<VendingMachine> findByPosSerialNumber(String posSerialNumber);

    // Paginated methods
    Page<VendingMachine> findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(
            String brand, String model, Pageable pageable);

    @Query("SELECT vm FROM VendingMachine vm WHERE vm.location.city = :city")
    Page<VendingMachine> findByLocationCity(@Param("city") String city, Pageable pageable);

    @Query("SELECT vm FROM VendingMachine vm WHERE vm.location.city = :city AND vm.active = :active")
    Page<VendingMachine> findByLocationCityAndActive(
            @Param("city") String city, @Param("active") Boolean active, Pageable pageable);

    Page<VendingMachine> findByActive(Boolean active, Pageable pageable);

    Optional<VendingMachine> findByMachineId(String machineId);

    boolean existsByMachineId(String machineId);
}
