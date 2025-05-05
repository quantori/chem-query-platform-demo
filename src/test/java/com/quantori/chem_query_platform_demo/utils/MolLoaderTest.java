package com.quantori.chem_query_platform_demo.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class MolLoaderTest {

    @Test
    void testGetQueryStructure_Success() throws IOException {
        String molContent = """
                Test mol file content
                line 2
                line 3
                """;

        Path tempMol = Files.createTempFile("test-", ".mol");
        Files.writeString(tempMol, molContent);

        String result = MolLoader.getQueryStructure(tempMol);

        assertEquals(molContent, result);
        Files.deleteIfExists(tempMol);
    }

    @Test
    void testGetQueryStructure_FileNotFound() {
        Path fakePath = Path.of("non_existing_file.mol");

        IOException ex = assertThrows(IOException.class, () -> MolLoader.getQueryStructure(fakePath));
        assertTrue(ex.getMessage().contains("File does not exist"));
    }

    @Test
    void testGetQueryStructure_InvalidExtension() throws IOException {
        Path invalid = Files.createTempFile("test-", ".txt");
        Files.writeString(invalid, "some text");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> MolLoader.getQueryStructure(invalid));

        assertTrue(ex.getMessage().contains("Expected .mol file"));
        Files.deleteIfExists(invalid);
    }
}
