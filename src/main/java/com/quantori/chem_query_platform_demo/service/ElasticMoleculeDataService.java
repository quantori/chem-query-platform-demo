package com.quantori.chem_query_platform_demo.service;

import com.quantori.cqp.api.model.Library;
import com.quantori.cqp.api.model.LibraryType;
import com.quantori.cqp.api.model.Property;
import com.quantori.cqp.api.service.StorageLibrary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ElasticMoleculeDataService implements MoleculeDataService {

    private final StorageLibrary storageLibrary;

    @Override
    public boolean create(String libraryName, Map<String, Property> propertyTypes) {
        return Objects.nonNull(storageLibrary.createLibrary(libraryName, LibraryType.molecules, propertyTypes,
                storageLibrary.getDefaultServiceData()));
    }

    @Override
    public List<String> getMoleculeLibraryIdsByName(List<String> libraryNames) {
        return libraryNames.stream().map(this::getLibraryByName).map(Library::getId).toList();
    }

    @Override
    public String getMoleculeLibraryIdByName(String name) {
        return getLibraryByName(name).getId();
    }

    private Library getLibraryByName(String name) {
        var libraryIterator = storageLibrary.getLibraryByName(name).iterator();
        if (libraryIterator.hasNext()) {
            return libraryIterator.next();
        }
        throw new NoSuchElementException("No library found with name: " + name);
    }
}
