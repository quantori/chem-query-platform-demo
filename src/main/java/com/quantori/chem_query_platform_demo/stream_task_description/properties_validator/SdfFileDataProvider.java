package com.quantori.chem_query_platform_demo.stream_task_description.properties_validator;

import static com.quantori.chem_query_platform_demo.parser.SDFParser.EMPTY_STRUCTURE;

import com.quantori.chem_query_platform_demo.parser.SdfStructure;
import com.quantori.chem_query_platform_demo.parser.SDFParser;
import com.quantori.chem_query_platform_demo.serde.DataWrapper;
import com.quantori.chem_query_platform_demo.serde.TransformIterator;
import com.quantori.cqp.core.task.model.DataProvider;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class SdfFileDataProvider implements DataProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SdfFileDataProvider.class);

    private final String filePath;
    private SDFParser sdfParser;

    public SdfFileDataProvider(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public Iterator<? extends Data> dataIterator() {
        Iterator<SdfStructure> sdfIterator = initializeParser();
        return new TransformIterator<>(sdfIterator, DataWrapper::new);
    }

    @Override
    public void close() {
        if (sdfParser == null) return;
        try {
            sdfParser.close();
            log.info("Close SDF parser");
        } catch (Exception e) {
            log.warn("Failed to close SDF parser for file: {}", filePath, e);
        }
    }

    private Iterator<SdfStructure> initializeParser() {
        try {

            sdfParser = new SDFParser(filePath);
        } catch (Exception e) {
            log.info("Unable to create parser for file: {}, error: {}", filePath, e.getMessage());
            return Collections.emptyIterator();
        }

        return new Iterator<>() {
            private final AtomicBoolean encounteredError = new AtomicBoolean(false);

            @Override
            public boolean hasNext() {
                try {
                    return !encounteredError.get() && sdfParser.hasNext();
                } catch (Exception e) {
                    encounteredError.set(true);
                    log.error("Error checking next element in file: {}", filePath, e);
                    return false;
                }
            }

            @Override
            public SdfStructure next() {
                try {
                    return sdfParser.next();
                } catch (Exception e) {
                    encounteredError.set(true);
                    LOGGER.error("Error reading next element in file: {}", filePath, e);
                    return new SdfStructure(EMPTY_STRUCTURE, Map.of(), e.getMessage());
                }
            }
        };
    }
}
