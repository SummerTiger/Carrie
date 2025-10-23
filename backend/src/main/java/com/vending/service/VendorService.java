package com.vending.service;

import com.vending.dto.VendorDTO;
import com.vending.entity.Vendor;
import com.vending.exception.ResourceNotFoundException;
import com.vending.repository.ProcurementBatchRepository;
import com.vending.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorService {

    private final VendorRepository vendorRepository;
    private final ProcurementBatchRepository procurementBatchRepository;

    @Cacheable(value = "vendors", key = "'all'")
    public List<VendorDTO> getAllVendors() {
        return vendorRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "vendors", key = "'active'")
    public List<VendorDTO> getActiveVendors() {
        return vendorRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "vendors", key = "'preferred'")
    public List<VendorDTO> getPreferredVendors() {
        return vendorRepository.findByPreferredTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "vendors", key = "#id")
    public VendorDTO getVendorById(UUID id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));
        return convertToDTO(vendor);
    }

    public List<VendorDTO> searchVendors(String search) {
        return vendorRepository.searchVendors(search).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "vendors", allEntries = true)
    public VendorDTO createVendor(VendorDTO vendorDTO) {
        Vendor vendor = convertToEntity(vendorDTO);
        vendor.setCreatedAt(LocalDateTime.now());
        vendor.setUpdatedAt(LocalDateTime.now());

        Vendor savedVendor = vendorRepository.save(vendor);
        log.info("Created new vendor: {} (ID: {})", savedVendor.getName(), savedVendor.getId());

        return convertToDTO(savedVendor);
    }

    @Transactional
    @CacheEvict(value = "vendors", allEntries = true)
    public VendorDTO updateVendor(UUID id, VendorDTO vendorDTO) {
        Vendor existingVendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));

        updateVendorFromDTO(existingVendor, vendorDTO);
        existingVendor.setUpdatedAt(LocalDateTime.now());

        Vendor updatedVendor = vendorRepository.save(existingVendor);
        log.info("Updated vendor: {} (ID: {})", updatedVendor.getName(), updatedVendor.getId());

        return convertToDTO(updatedVendor);
    }

    @Transactional
    @CacheEvict(value = "vendors", allEntries = true)
    public void deleteVendor(UUID id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));

        vendorRepository.delete(vendor);
        log.info("Deleted vendor: {} (ID: {})", vendor.getName(), vendor.getId());
    }

    @Transactional
    @CacheEvict(value = "vendors", allEntries = true)
    public VendorDTO toggleVendorStatus(UUID id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));

        vendor.setActive(!vendor.getActive());
        vendor.setUpdatedAt(LocalDateTime.now());

        Vendor updatedVendor = vendorRepository.save(vendor);
        log.info("Toggled vendor status: {} - Active: {}", updatedVendor.getName(), updatedVendor.getActive());

        return convertToDTO(updatedVendor);
    }

    private VendorDTO convertToDTO(Vendor vendor) {
        VendorDTO dto = VendorDTO.builder()
                .id(vendor.getId())
                .name(vendor.getName())
                .companyName(vendor.getCompanyName())
                .contactPerson(vendor.getContactPerson())
                .email(vendor.getEmail())
                .phoneNumber(vendor.getPhoneNumber())
                .mobileNumber(vendor.getMobileNumber())
                .faxNumber(vendor.getFaxNumber())
                .website(vendor.getWebsite())
                .addressLine1(vendor.getAddressLine1())
                .addressLine2(vendor.getAddressLine2())
                .city(vendor.getCity())
                .province(vendor.getProvince())
                .postalCode(vendor.getPostalCode())
                .country(vendor.getCountry())
                .customerIdWithVendor(vendor.getCustomerIdWithVendor())
                .businessNumber(vendor.getBusinessNumber())
                .taxId(vendor.getTaxId())
                .paymentTerms(vendor.getPaymentTerms())
                .discountRate(vendor.getDiscountRate())
                .creditLimit(vendor.getCreditLimit())
                .productCategories(vendor.getProductCategories())
                .description(vendor.getDescription())
                .notes(vendor.getNotes())
                .active(vendor.getActive())
                .preferred(vendor.getPreferred())
                .rating(vendor.getRating())
                .orderDeliver(vendor.getOrderDeliver())
                .curbsidePickup(vendor.getCurbsidePickup())
                .inPersonOnly(vendor.getInPersonOnly())
                .createdAt(vendor.getCreatedAt())
                .updatedAt(vendor.getUpdatedAt())
                .build();

        // Calculate purchase statistics
        var batches = procurementBatchRepository.findByVendorId(vendor.getId());
        dto.setTotalPurchases(batches.size());

        BigDecimal totalSpent = batches.stream()
                .map(batch -> batch.getTotalAmount() != null ? batch.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalSpent(totalSpent);

        batches.stream()
                .map(batch -> batch.getPurchaseDate())
                .max(LocalDateTime::compareTo)
                .ifPresent(dto::setLastPurchaseDate);

        return dto;
    }

    private Vendor convertToEntity(VendorDTO dto) {
        return Vendor.builder()
                .name(dto.getName())
                .companyName(dto.getCompanyName())
                .contactPerson(dto.getContactPerson())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .mobileNumber(dto.getMobileNumber())
                .faxNumber(dto.getFaxNumber())
                .website(dto.getWebsite())
                .addressLine1(dto.getAddressLine1())
                .addressLine2(dto.getAddressLine2())
                .city(dto.getCity())
                .province(dto.getProvince())
                .postalCode(dto.getPostalCode())
                .country(dto.getCountry())
                .customerIdWithVendor(dto.getCustomerIdWithVendor())
                .businessNumber(dto.getBusinessNumber())
                .taxId(dto.getTaxId())
                .paymentTerms(dto.getPaymentTerms())
                .discountRate(dto.getDiscountRate())
                .creditLimit(dto.getCreditLimit())
                .productCategories(dto.getProductCategories())
                .description(dto.getDescription())
                .notes(dto.getNotes())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .preferred(dto.getPreferred() != null ? dto.getPreferred() : false)
                .rating(dto.getRating() != null ? dto.getRating() : 0)
                .orderDeliver(dto.getOrderDeliver() != null ? dto.getOrderDeliver() : false)
                .curbsidePickup(dto.getCurbsidePickup() != null ? dto.getCurbsidePickup() : false)
                .inPersonOnly(dto.getInPersonOnly() != null ? dto.getInPersonOnly() : false)
                .build();
    }

    private void updateVendorFromDTO(Vendor vendor, VendorDTO dto) {
        vendor.setName(dto.getName());
        vendor.setCompanyName(dto.getCompanyName());
        vendor.setContactPerson(dto.getContactPerson());
        vendor.setEmail(dto.getEmail());
        vendor.setPhoneNumber(dto.getPhoneNumber());
        vendor.setMobileNumber(dto.getMobileNumber());
        vendor.setFaxNumber(dto.getFaxNumber());
        vendor.setWebsite(dto.getWebsite());
        vendor.setAddressLine1(dto.getAddressLine1());
        vendor.setAddressLine2(dto.getAddressLine2());
        vendor.setCity(dto.getCity());
        vendor.setProvince(dto.getProvince());
        vendor.setPostalCode(dto.getPostalCode());
        vendor.setCountry(dto.getCountry());
        vendor.setCustomerIdWithVendor(dto.getCustomerIdWithVendor());
        vendor.setBusinessNumber(dto.getBusinessNumber());
        vendor.setTaxId(dto.getTaxId());
        vendor.setPaymentTerms(dto.getPaymentTerms());
        vendor.setDiscountRate(dto.getDiscountRate());
        vendor.setCreditLimit(dto.getCreditLimit());
        vendor.setProductCategories(dto.getProductCategories());
        vendor.setDescription(dto.getDescription());
        vendor.setNotes(dto.getNotes());

        if (dto.getActive() != null) {
            vendor.setActive(dto.getActive());
        }
        if (dto.getPreferred() != null) {
            vendor.setPreferred(dto.getPreferred());
        }
        if (dto.getRating() != null) {
            vendor.setRating(dto.getRating());
        }
        if (dto.getOrderDeliver() != null) {
            vendor.setOrderDeliver(dto.getOrderDeliver());
        }
        if (dto.getCurbsidePickup() != null) {
            vendor.setCurbsidePickup(dto.getCurbsidePickup());
        }
        if (dto.getInPersonOnly() != null) {
            vendor.setInPersonOnly(dto.getInPersonOnly());
        }
    }
}
