package com.quantori.chem_query_platform_demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quantori.cqp.api.model.Library;
import com.quantori.cqp.api.model.LibraryType;
import com.quantori.cqp.api.model.Property;
import com.quantori.cqp.api.service.StorageLibrary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

class ElasticMoleculeDataServiceTest {

    private StorageLibrary storageLibrary;
    private ElasticMoleculeDataService moleculeStorage;

    @BeforeEach
    void setUp() {
        storageLibrary = mock(StorageLibrary.class);
        moleculeStorage = new ElasticMoleculeDataService(storageLibrary);
    }

    @Test
    void testCreate_Successful() {
        String libraryName = "TestLib";
        Map<String, Property> props = new HashMap<>();

        when(storageLibrary.getDefaultServiceData()).thenReturn(Map.of());
        when(storageLibrary.createLibrary(eq(libraryName), eq(LibraryType.molecules), eq(props), eq(Map.of())))
                .thenReturn(new Library());

        boolean result = moleculeStorage.create(libraryName, props);

        assertTrue(result);
    }

    @Test
    void testCreate_Failure() {
        String libraryName = "TestLib";
        Map<String, Property> props = new HashMap<>();

        when(storageLibrary.getDefaultServiceData()).thenReturn(Map.of());
        when(storageLibrary.createLibrary(any(), any(), any(), any())).thenReturn(null);

        boolean result = moleculeStorage.create(libraryName, props);

        assertFalse(result);
    }

    @Test
    void testGetMoleculeLibraryIdByName_Success() {
        String name = "TestLib";
        String id = "abc123";

        Library lib = new Library();
        lib.setId(id);

        when(storageLibrary.getLibraryByName(name)).thenReturn(List.of(lib));

        String result = moleculeStorage.getMoleculeLibraryIdByName(name);

        assertEquals(id, result);
    }

    @Test
    void testGetMoleculeLibraryIdByName_NotFound() {
        when(storageLibrary.getLibraryByName("Unknown")).thenReturn(Collections.emptyList());

        assertThrows(NoSuchElementException.class, () ->
                moleculeStorage.getMoleculeLibraryIdByName("Unknown"));
    }

    @Test
    void testGetMoleculeLibraryIdsByName_Success() {
        Library lib1 = new Library();
        lib1.setId("id1");
        Library lib2 = new Library();
        lib2.setId("id2");

        when(storageLibrary.getLibraryByName("Lib1")).thenReturn(List.of(lib1));
        when(storageLibrary.getLibraryByName("Lib2")).thenReturn(List.of(lib2));

        List<String> result = moleculeStorage.getMoleculeLibraryIdsByName(List.of("Lib1", "Lib2"));

        assertEquals(List.of("id1", "id2"), result);
    }
}
