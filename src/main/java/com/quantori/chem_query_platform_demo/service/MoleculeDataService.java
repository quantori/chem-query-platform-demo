package com.quantori.chem_query_platform_demo.service;

import com.quantori.cqp.api.model.Property;

import java.util.List;
import java.util.Map;

public interface MoleculeDataService {
    boolean create(String libraryName, Map<String, Property> propertyTypes);

    List<String> getMoleculeLibraryIdsByName(List<String> libraryNames);

    String getMoleculeLibraryIdByName(String name);
}
