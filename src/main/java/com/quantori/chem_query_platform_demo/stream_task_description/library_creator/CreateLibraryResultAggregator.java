package com.quantori.chem_query_platform_demo.stream_task_description.library_creator;

import com.quantori.chem_query_platform_demo.service.MoleculeLibraryException;
import com.quantori.chem_query_platform_demo.service.ElasticMoleculeDataService;
import com.quantori.cqp.api.model.Property;
import com.quantori.cqp.core.task.model.DataProvider;
import com.quantori.cqp.core.task.model.ResultAggregator;
import com.quantori.cqp.core.task.model.StreamTaskResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class CreateLibraryResultAggregator implements ResultAggregator {
    private final String fileName;
    private final Map<String, Property> properties;
    private final ElasticMoleculeDataService elasticMoleculeStorage;

    @Override
    public void consume(DataProvider.Data data) {
        if (!elasticMoleculeStorage.create(fileName, properties)) {
            throw new MoleculeLibraryException("Failed to create library for uploading ");
        }
    }

    @Override
    public StreamTaskResult getResult() {
        return new TaskResult(properties);
    }

    @Override
    public double getPercent() {
        return 0;
    }
}
