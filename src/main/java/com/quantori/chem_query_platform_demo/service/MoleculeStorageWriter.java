package com.quantori.chem_query_platform_demo.service;

import com.quantori.cqp.api.model.upload.Molecule;
import com.quantori.cqp.api.service.StorageMolecules;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MoleculeStorageWriter {

    private final StorageMolecules storageMolecules;
    private final MoleculeDataService moleculeDataService;

    public void add(List<Molecule> molecules, final String libraryId) {
        String libraryName = moleculeDataService.getMoleculeLibraryIdByName(libraryId);
        try (var writer = storageMolecules.itemWriter(libraryName)) {
            molecules.forEach(writer::write);
        } catch (Exception e) {
            log.error("Failed to write molecules to storage", e);
        }
    }
}
