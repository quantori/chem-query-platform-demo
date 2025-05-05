package com.quantori.chem_query_platform_demo.parser;

import java.util.function.Function;

public enum SDFParserState {
    START,
    STRUCTURE,
    STRUCTURE_END,
    PROPERTY_NAME,
    PROPERTY_EMPTY_VALUE,
    PROPERTY_VALUE,
    PROPERTY_END,
    END,
    ERROR;

    private Function<String, SDFParserState> transition;

    static {
        START.transition = line -> STRUCTURE;

        STRUCTURE.transition = line -> {
            String t = line.trim();
            return t.endsWith("END") ? STRUCTURE_END : STRUCTURE;
        };

        STRUCTURE_END.transition = line -> {
            String t = line.trim();
            if (t.startsWith("$$$$")) return END;
            else if (t.startsWith(">")) return PROPERTY_NAME;
            else if (!t.isEmpty()) return ERROR;
            else return STRUCTURE_END;
        };

        PROPERTY_NAME.transition = line -> {
            String t = line.trim();
            return t.isEmpty() ? PROPERTY_EMPTY_VALUE : PROPERTY_VALUE;
        };

        PROPERTY_EMPTY_VALUE.transition = line -> {
            String t = line.trim();
            if (t.startsWith(">")) return PROPERTY_NAME;
            else if (t.isEmpty()) return PROPERTY_END;
            else return PROPERTY_VALUE;
        };

        PROPERTY_VALUE.transition = line -> {
            String t = line.trim();
            return t.isEmpty() ? PROPERTY_END : PROPERTY_VALUE;
        };

        PROPERTY_END.transition = line -> {
            String t = line.trim();
            if (t.startsWith("$$$$")) return END;
            else if (t.startsWith(">")) return PROPERTY_NAME;
            else if (!t.isEmpty()) return ERROR;
            else return PROPERTY_END;
        };

        END.transition = line -> START;

        ERROR.transition = line -> {
            String t = line.trim();
            return t.startsWith("$$$$") ? END : ERROR;
        };
    }

    /**
     * Returns the next parser state based on the input line.
     *
     * @param line a line of the SDF input (with potential leading/trailing whitespace)
     * @return the next SDFParserState
     */
    public SDFParserState nextState(String line) {
        return transition.apply(line);
    }
}
