package com.vending.service;

import com.vending.entity.MachineBrand;
import com.vending.entity.MachineModel;
import com.vending.exception.DuplicateResourceException;
import com.vending.exception.ResourceNotFoundException;
import com.vending.repository.MachineBrandRepository;
import com.vending.repository.MachineModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MachineModelService {

    private final MachineModelRepository modelRepository;
    private final MachineBrandRepository brandRepository;

    public List<MachineModel> getAllModels() {
        return modelRepository.findAll();
    }

    public List<MachineModel> getAllActiveModels() {
        return modelRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }

    public Page<MachineModel> getAllModels(Pageable pageable) {
        return modelRepository.findAll(pageable);
    }

    public Page<MachineModel> searchModels(String name, Pageable pageable) {
        return modelRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    public MachineModel getModelById(UUID id) {
        return modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MachineModel", "id", id));
    }

    public List<MachineModel> getModelsByBrand(UUID brandId) {
        MachineBrand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("MachineBrand", "id", brandId));
        return modelRepository.findByMachineBrandAndActiveTrue(brand);
    }

    public MachineModel createModel(MachineModel model) {
        if (model.getMachineBrand() != null && model.getMachineBrand().getId() != null) {
            MachineBrand brand = brandRepository.findById(model.getMachineBrand().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("MachineBrand", "id", model.getMachineBrand().getId()));

            if (modelRepository.findByMachineBrandAndName(brand, model.getName()).isPresent()) {
                throw new DuplicateResourceException("MachineModel", "name", model.getName() + " for brand " + brand.getName());
            }
            model.setMachineBrand(brand);
        }
        return modelRepository.save(model);
    }

    public MachineModel updateModel(UUID id, MachineModel modelUpdate) {
        MachineModel model = getModelById(id);

        if (modelUpdate.getMachineBrand() != null && modelUpdate.getMachineBrand().getId() != null) {
            MachineBrand brand = brandRepository.findById(modelUpdate.getMachineBrand().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("MachineBrand", "id", modelUpdate.getMachineBrand().getId()));
            model.setMachineBrand(brand);
        }

        if (modelUpdate.getName() != null) {
            // Only check for duplicates if the name is actually changing
            if (!model.getName().equals(modelUpdate.getName()) && model.getMachineBrand() != null &&
                modelRepository.findByMachineBrandAndName(model.getMachineBrand(), modelUpdate.getName()).isPresent()) {
                throw new DuplicateResourceException("MachineModel", "name", modelUpdate.getName());
            }
            model.setName(modelUpdate.getName());
        }

        if (modelUpdate.getModelNumber() != null) model.setModelNumber(modelUpdate.getModelNumber());
        if (modelUpdate.getDescription() != null) model.setDescription(modelUpdate.getDescription());
        if (modelUpdate.getCapacity() != null) model.setCapacity(modelUpdate.getCapacity());
        if (modelUpdate.getDimensions() != null) model.setDimensions(modelUpdate.getDimensions());
        if (modelUpdate.getWeightKg() != null) model.setWeightKg(modelUpdate.getWeightKg());
        if (modelUpdate.getPowerRequirements() != null) model.setPowerRequirements(modelUpdate.getPowerRequirements());
        if (modelUpdate.getActive() != null) model.setActive(modelUpdate.getActive());
        if (modelUpdate.getDisplayOrder() != null) model.setDisplayOrder(modelUpdate.getDisplayOrder());

        return modelRepository.save(model);
    }

    public void deleteModel(UUID id) {
        if (!modelRepository.existsById(id)) {
            throw new ResourceNotFoundException("MachineModel", "id", id);
        }
        modelRepository.deleteById(id);
    }
}
