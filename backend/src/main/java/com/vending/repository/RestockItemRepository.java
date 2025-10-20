package com.vending.repository;

import com.vending.entity.Product;
import com.vending.entity.RestockItem;
import com.vending.entity.RestockingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RestockItemRepository extends JpaRepository<RestockItem, UUID> {

    List<RestockItem> findByRestockingLog(RestockingLog restockingLog);

    List<RestockItem> findByProduct(Product product);

    List<RestockItem> findByProductId(UUID productId);
}
