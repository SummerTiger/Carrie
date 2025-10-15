package com.vending.service;

import com.vending.dto.ProcurementBatchDto;
import com.vending.dto.ProcurementItemDto;
import com.vending.entity.ProcurementBatch;
import com.vending.entity.ProcurementItem;
import com.vending.entity.Product;
import com.vending.repository.ProcurementBatchRepository;
import com.vending.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcurementBatchService {

    private final ProcurementBatchRepository procurementBatchRepository;
    private final ProductRepository productRepository;

    @Transactional
    public List<ProcurementBatchDto> getAllBatches() {
        return procurementBatchRepository.findAllOrderByPurchaseDateDesc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProcurementBatchDto getBatchById(UUID id) {
        ProcurementBatch batch = procurementBatchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Procurement batch not found with id: " + id));
        return toDto(batch);
    }

    @Transactional
    public List<ProcurementBatchDto> getBatchesBySupplier(String supplier) {
        return procurementBatchRepository.findBySupplier(supplier)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<String> getAllSuppliers() {
        return procurementBatchRepository.findAllSuppliers();
    }

    @Transactional
    public ProcurementBatchDto createBatch(ProcurementBatchDto dto) {
        ProcurementBatch batch = ProcurementBatch.builder()
                .purchaseDate(dto.purchaseDate() != null ? dto.purchaseDate() : LocalDateTime.now())
                .supplier(dto.supplier())
                .supplierContact(dto.supplierContact())
                .invoiceNumber(dto.invoiceNumber())
                .notes(dto.notes())
                .build();

        // Add items
        if (dto.items() != null) {
            for (ProcurementItemDto itemDto : dto.items()) {
                Product product = productRepository.findById(itemDto.productId())
                        .orElseThrow(() -> new RuntimeException("Product not found with id: " + itemDto.productId()));

                ProcurementItem item = ProcurementItem.builder()
                        .product(product)
                        .quantity(itemDto.quantity())
                        .unitCost(itemDto.unitCost())
                        .hstExempt(itemDto.hstExempt())
                        .build();

                batch.addItem(item);
            }
        }

        ProcurementBatch saved = procurementBatchRepository.save(batch);

        // Update product stock quantities
        updateProductStock(saved);

        return toDto(saved);
    }

    @Transactional
    public ProcurementBatchDto updateBatch(UUID id, ProcurementBatchDto dto) {
        ProcurementBatch batch = procurementBatchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Procurement batch not found with id: " + id));

        batch.setPurchaseDate(dto.purchaseDate());
        batch.setSupplier(dto.supplier());
        batch.setSupplierContact(dto.supplierContact());
        batch.setInvoiceNumber(dto.invoiceNumber());
        batch.setNotes(dto.notes());

        // Clear existing items
        batch.getItems().clear();

        // Add new items
        if (dto.items() != null) {
            for (ProcurementItemDto itemDto : dto.items()) {
                Product product = productRepository.findById(itemDto.productId())
                        .orElseThrow(() -> new RuntimeException("Product not found with id: " + itemDto.productId()));

                ProcurementItem item = ProcurementItem.builder()
                        .product(product)
                        .quantity(itemDto.quantity())
                        .unitCost(itemDto.unitCost())
                        .hstExempt(itemDto.hstExempt())
                        .build();

                batch.addItem(item);
            }
        }

        ProcurementBatch updated = procurementBatchRepository.save(batch);
        return toDto(updated);
    }

    @Transactional
    public void deleteBatch(UUID id) {
        if (!procurementBatchRepository.existsById(id)) {
            throw new RuntimeException("Procurement batch not found with id: " + id);
        }
        procurementBatchRepository.deleteById(id);
    }

    private void updateProductStock(ProcurementBatch batch) {
        for (ProcurementItem item : batch.getItems()) {
            Product product = item.getProduct();
            int newStock = product.getCurrentStock() + item.getQuantity();
            product.setCurrentStock(newStock);
            productRepository.save(product);
        }
    }

    private ProcurementBatchDto toDto(ProcurementBatch batch) {
        List<ProcurementItemDto> itemDtos = batch.getItems().stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());

        return ProcurementBatchDto.builder()
                .id(batch.getId())
                .purchaseDate(batch.getPurchaseDate())
                .supplier(batch.getSupplier())
                .supplierContact(batch.getSupplierContact())
                .items(itemDtos)
                .totalHst(batch.getTotalHst())
                .subtotal(batch.getSubtotal())
                .totalAmount(batch.getTotalAmount())
                .invoiceNumber(batch.getInvoiceNumber())
                .notes(batch.getNotes())
                .totalItemsCount(batch.getTotalItemsCount())
                .createdAt(batch.getCreatedAt())
                .build();
    }

    private ProcurementItemDto toItemDto(ProcurementItem item) {
        return ProcurementItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitCost(item.getUnitCost())
                .hstAmount(item.getHstAmount())
                .hstExempt(item.isHstExempt())
                .totalCost(item.getTotalCost())
                .totalWithHst(item.getTotalWithHst())
                .build();
    }
}
