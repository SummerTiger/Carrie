package com.vending.repository;

import com.vending.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, UUID> {

    List<Vendor> findByActiveTrue();

    List<Vendor> findByPreferredTrue();

    List<Vendor> findByActiveTrueAndPreferredTrue();

    Optional<Vendor> findByCustomerIdWithVendor(String customerIdWithVendor);

    Optional<Vendor> findByNameIgnoreCase(String name);

    @Query("SELECT v FROM Vendor v WHERE " +
           "LOWER(v.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Vendor> searchVendors(String search);

    @Query("SELECT v FROM Vendor v WHERE v.active = true ORDER BY v.name")
    List<Vendor> findAllActiveOrderByName();
}
