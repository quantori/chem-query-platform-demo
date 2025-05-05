package com.quantori.chem_query_platform_demo.stream_task_description.library_creator;

import static com.quantori.chem_query_platform_demo.parser.SDFParser.EMPTY_STRUCTURE;

import com.quantori.chem_query_platform_demo.parser.SdfStructure;
import com.quantori.chem_query_platform_demo.parser.exception.ParserException;
import com.quantori.chem_query_platform_demo.parser.SDFParser;
import com.quantori.chem_query_platform_demo.serde.DataWrapper;
import com.quantori.chem_query_platform_demo.serde.TransformIterator;
import com.quantori.cqp.core.task.model.DataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SdfFileListDataProvider implements DataProvider {

    private final String filePath;
    private SDFParser sdfParser;

    @Override
    public Iterator<? extends Data> dataIterator() {
        Iterator<List<SdfStructure>> sdfIterator = createIterator();
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

    private Iterator<List<SdfStructure>> createIterator() {

        try {
            sdfParser = new SDFParser(filePath);
        } catch (ParserException e) {
            log.warn("Cannot iterate mol file: {}, error: {}", filePath, e.getMessage());
            return Collections.emptyIterator();
        }
        return new Iterator<>() {

            @Override
            public boolean hasNext() {
                return sdfParser.hasNext();
            }

            @Override
            public List<SdfStructure> next() {
                return nextMolecules();
            }

            private List<SdfStructure> nextMolecules() {
                List<SdfStructure> batch = new ArrayList<>();
                while (batch.size() < 500 && sdfParser.hasNext()) {
                    batch.add(nextMolecule());
                }
                return batch;
            }

            private SdfStructure nextMolecule() {
                try {
                    return sdfParser.next();
                } catch (Exception e) {
                    log.error("exception", e);
                    return getBadMolecule(e);
                }
            }

            private SdfStructure getBadMolecule(Exception e) {
                String message = e.getMessage() == null ? "error" : e.getMessage();
                return new SdfStructure(EMPTY_STRUCTURE, Map.of(), message);
            }
        };
    }
}
