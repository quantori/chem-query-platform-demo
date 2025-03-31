package com.quantori.chem_query_platform_demo.parser;

import com.quantori.chem_query_platform_demo.parser.exception.FileParserException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Iterator-based SDF file parser that lazily parses each molecule's properties
 * into a {@link Molecule}, containing a map of key-value metadata pairs.
 */
public class SDFParser implements Iterator<SDFParser.Molecule>, Closeable {

    private static final String DELIMITER = "$$$$";

    private final BufferedReader reader;
    private Molecule nextMolecule;
    private boolean finished = false;

    public SDFParser(String absolutePath) {
        try {
            this.reader = Files.newBufferedReader(Path.of(absolutePath));
            this.nextMolecule = parseNext();
        } catch (IOException e) {
            throw new FileParserException("Failed to open SDF file: " + absolutePath, e);
        }
    }

    @Override
    public boolean hasNext() {
        return nextMolecule != null;
    }

    @Override
    public Molecule next() {
        if (nextMolecule == null) {
            throw new NoSuchElementException("No more molecules in the SDF file.");
        }
        Molecule current = nextMolecule;
        nextMolecule = parseNext();
        return current;
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            throw new FileParserException("Failed to close SDF reader", e);
        }
    }

    private Molecule parseNext() {
        if (finished) return null;

        List<String> blockLines = readUntilDelimiter();
        if (blockLines.isEmpty()) {
            finished = true;
            return null;
        }

        return new Molecule(extractProperties(blockLines));
    }

    private List<String> readUntilDelimiter() {
        List<String> blockLines = new ArrayList<>();
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                if (line.equals(DELIMITER)) break;
                blockLines.add(line);
            }
        } catch (IOException e) {
            finished = true;
            throw new FileParserException("Error reading SDF stream", e);
        }

        return blockLines;
    }

    private Map<String, String> extractProperties(List<String> blockLines) {
        Map<String, String> props = new LinkedHashMap<>();
        int i = skipMoleculeBlock(blockLines);

        while (i < blockLines.size()) {
            String line = blockLines.get(i);
            if (line.startsWith(">")) {
                String key = extractKey(line);
                StringBuilder value = new StringBuilder();
                i++;
                while (i < blockLines.size() && !blockLines.get(i).startsWith(">") && !blockLines.get(i).equals(DELIMITER)) {
                    value.append(blockLines.get(i)).append("\n");
                    i++;
                }
                props.put(key, value.toString().trim());
            } else {
                i++;
            }
        }

        return props;
    }

    private int skipMoleculeBlock(List<String> blockLines) {
        int i = 0;
        while (i < blockLines.size() && !blockLines.get(i).startsWith(">")) {
            i++;
        }
        return i;
    }

    private String extractKey(String tagLine) {
        int start = tagLine.indexOf('<');
        int end = tagLine.indexOf('>');
        if (start != -1 && end != -1 && end > start + 1) {
            return tagLine.substring(start + 1, end).trim();
        }
        return "UNKNOWN";
    }

    /**
     * Represents a parsed molecule from an SDF file.
     * Contains only the metadata properties (tag-value pairs).
     */
    public record Molecule(Map<String, String> properties) {
        public String getProperty(String key) {
            return properties.get(key);
        }
    }
}
