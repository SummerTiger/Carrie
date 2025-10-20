package com.vending.repository;

import com.vending.entity.ProcurementBatch;
import com.vending.entity.ProcurementItem;
import com.vending.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProcurementItemRepository extends JpaRepository<ProcurementItem, UUID> {

    List<ProcurementItem> findByBatch(ProcurementBatch batch);

    List<ProcurementItem> findByProduct(Product product);

    List<ProcurementItem> findByProductId(UUID productId);
}
