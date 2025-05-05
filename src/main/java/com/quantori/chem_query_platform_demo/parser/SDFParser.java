package com.quantori.chem_query_platform_demo.parser;

import com.quantori.chem_query_platform_demo.parser.exception.ParserException;

import java.io.Closeable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Parses an SDF (Structure Data File) file into individual ParsedSdfStructure objects.
 */
public class SDFParser implements Iterator<SdfStructure>, Closeable {

    public static final String EMPTY_STRUCTURE = """
            EMPTY
             -ISIS-  09302110142D
             
              0  0  0  0  0  0  0  0  0  0999 V2000
            M  END
            """;

    private static final int ERROR_THRESHOLD = 1000;
    private static final String GENERIC_ERROR = "Error!";

    private final FileLinesScanner scanner;
    private SdfStructure nextMolecule;
    private long lineCount = 0;
    private long errorCount = 0;
    private SDFParserState state = SDFParserState.START;

    public SDFParser(String filePath) {
        this.scanner = new FileLinesScanner(filePath);
    }

    @Override
    public boolean hasNext() {
        if (nextMolecule == null) {
            nextMolecule = readNextMolecule();
        }
        return nextMolecule != null;
    }

    @Override
    public SdfStructure next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more molecules to read.");
        }
        SdfStructure current = nextMolecule;
        nextMolecule = null;
        return current;
    }

    @Override
    public void close() {
        scanner.close();
    }

    private SdfStructure readNextMolecule() {
        StringBuilder structureBuilder = new StringBuilder();
        Map<String, String> properties = new LinkedHashMap<>();
        String propertyName = null;
        StringBuilder propertyValue = new StringBuilder();
        String errorMessage = null;

        while (!state.equals(SDFParserState.END) && scanner.hasNext()) {
            String line = scanner.next();
            lineCount++;
            state = state.nextState(line);

            switch (state) {
                case STRUCTURE, STRUCTURE_END -> structureBuilder.append(line).append(System.lineSeparator());

                case PROPERTY_NAME -> {
                    if (propertyName != null) {
                        properties.put(propertyName, propertyValue.toString());
                        propertyValue = new StringBuilder();
                    }
                    try {
                        propertyName = extractPropertyName(line);
                    } catch (ParserException e) {
                        propertyName = null;
                        errorMessage = formatErrorMessage(e.getMessage());
                    }
                }

                case PROPERTY_VALUE, PROPERTY_EMPTY_VALUE -> propertyValue.append(line).append(System.lineSeparator());

                case PROPERTY_END -> {
                    String cleanedValue = cleanValue(propertyValue.toString());
                    if (propertyName != null) {
                        properties.merge(propertyName, cleanedValue, (oldVal, newVal) -> oldVal + " " + newVal);
                        propertyName = null;
                        propertyValue = new StringBuilder();
                    }
                }

                case ERROR -> {
                    if (errorMessage == null) {
                        errorMessage = formatErrorMessage("Unrecognized line: " + line);
                    }
                }

                case END -> {
                    return createParsedStructure(structureBuilder, properties, errorMessage);
                }

                default -> {
                    // Ignore other states (START, etc.)
                }
            }
        }

        return finishPartialStructure(structureBuilder, properties, errorMessage);
    }

    private SdfStructure createParsedStructure(StringBuilder structure,
                                               Map<String, String> properties,
                                               String error) {
        String structureText = structure.toString();
        String finalError = errorCount < ERROR_THRESHOLD ? error : GENERIC_ERROR;
        if (finalError != null) {
            errorCount++;
        }
        state = SDFParserState.START;
        return new SdfStructure(structureText, properties, finalError);
    }

    private SdfStructure finishPartialStructure(StringBuilder structure,
                                                Map<String, String> properties,
                                                String error) {
        if (!state.equals(SDFParserState.START)) {
            if (state.equals(SDFParserState.STRUCTURE) && structure.toString().isBlank()) {
                return null;
            }
            String message = error != null ? error : "Molecule content incomplete";
            errorCount++;
            return new SdfStructure(EMPTY_STRUCTURE, properties,
                    errorCount < ERROR_THRESHOLD ? message : GENERIC_ERROR);
        }
        return null;
    }

    private String formatErrorMessage(String message) {
        return String.format("%s, line %d", message, lineCount);
    }

    private static String cleanValue(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private static String extractPropertyName(String line) {
        int start = line.indexOf("<");
        int end = line.indexOf(">", start);
        if (start < 1 || end - start < 2) {
            throw new ParserException("Invalid property name syntax: " + line);
        }
        return line.substring(start + 1, end);
    }
}
