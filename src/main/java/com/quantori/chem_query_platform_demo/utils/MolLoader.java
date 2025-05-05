package com.quantori.chem_query_platform_demo.utils;

import lombok.experimental.UtilityClass;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

@UtilityClass
public class MolLoader {

    private static final String MOL_EXTENSION = ".mol";

    /**
     * Loads molecule structure from a .mol file.
     *
     * @param pathToMolFile absolute or relative file path
     * @return file content as string
     * @throws IOException if file can't be read
     * @throws IllegalArgumentException if the file is not a .mol file
     */
    public static String getQueryStructure(Path pathToMolFile) throws IOException {

        if (!Files.exists(pathToMolFile)) {
            throw new IOException("File does not exist: " + pathToMolFile);
        }

        if (!pathToMolFile.getFileName().toString().toLowerCase().endsWith(MOL_EXTENSION)) {
            throw new IllegalArgumentException("Invalid file extension. Expected .mol file: " + pathToMolFile);
        }

        return Files.readString(pathToMolFile);
    }
}

