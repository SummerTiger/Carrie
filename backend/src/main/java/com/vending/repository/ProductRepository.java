package com.vending.repository;

import com.vending.entity.Product;
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
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByName(String name);

    Optional<Product> findByBarcode(String barcode);

    Optional<Product> findBySku(String sku);

    List<Product> findByCategory(String category);

    List<Product> findByActiveTrue();

    List<Product> findByCategoryAndActiveTrue(String category);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.currentStock < p.minimumStock")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.currentStock = 0")
    List<Product> findOutOfStockProducts();

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.currentStock <= :threshold")
    List<Product> findProductsBelowStockThreshold(@Param("threshold") int threshold);

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.active = true ORDER BY p.category")
    List<String> findAllCategories();

    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.category) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true")
    long countActiveProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true AND p.currentStock < p.minimumStock")
    long countLowStockProducts();

    @Query("SELECT SUM(p.currentStock) FROM Product p WHERE p.active = true")
    Long getTotalStockCount();

    List<Product> findByHstExempt(boolean hstExempt);

    // Paginated methods
    Page<Product> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            String name, String category, Pageable pageable);

    Page<Product> findByCategory(String category, Pageable pageable);

    Page<Product> findByCategoryAndActive(String category, Boolean active, Pageable pageable);

    Page<Product> findByActive(Boolean active, Pageable pageable);
}
