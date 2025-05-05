package com.quantori.chem_query_platform_demo.parser;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Represents a molecule read from SDF (Structure Data File) format.
 */
public record SdfStructure(String structure, Map<String, String> properties, String error) {

    private static final Pattern CONTAINS_ALPHA = Pattern.compile(".*[a-zA-Z]+.*");
    private static final Pattern COMMA = Pattern.compile(",");
    private static final String STRUCTURE_END = "$$$$";

    /**
     * Get formatted string of molecule structure.
     *
     * @return String representation of structure.
     */
    public String getMoleculeString() {
        return structure() + toSdfPropertiesString(properties) + STRUCTURE_END;
    }

    /**
     * Converts molecule properties to the SDF property string format.
     *
     * @param properties molecule properties.
     * @return string representation of molecule properties in SDF format.
     */
    private static String toSdfPropertiesString(Map<String, String> properties) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = cleanValue(entry.getValue());

            sb.append(">  <").append(key).append(">")
                    .append(System.lineSeparator())
                    .append(value).append(System.lineSeparator())
                    .append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     * Removes commas from purely numeric strings. Leaves alphanumeric strings unchanged.
     *
     * @param value input value
     * @return cleaned value
     */
    private static String cleanValue(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.contains(",") && !CONTAINS_ALPHA.matcher(value).matches()
                ? COMMA.matcher(value).replaceAll("")
                : value;
    }
}
