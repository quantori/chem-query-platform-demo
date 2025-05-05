package com.quantori.chem_query_platform_demo.stream_task_description.library_creator;

import static com.quantori.chem_query_platform_demo.constants.TaskFlowConstants.UPLOAD;
import static com.quantori.chem_query_platform_demo.constants.TaskFlowConstants.USER;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantori.chem_query_platform_demo.service.ElasticMoleculeDataService;
import com.quantori.chem_query_platform_demo.service.MoleculeLibraryException;
import com.quantori.cqp.api.model.Property;
import com.quantori.cqp.core.task.model.DataProvider;
import com.quantori.cqp.core.task.model.DescriptionState;
import com.quantori.cqp.core.task.model.ResumableTaskDescription;
import com.quantori.cqp.core.task.model.StreamTaskDescription;
import com.quantori.cqp.core.task.model.StreamTaskFunction;
import com.quantori.cqp.core.task.model.TaskDescriptionSerDe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class CreateLibraryTaskSerDe implements TaskDescriptionSerDe {
    public static final String UPLOAD_ID = "uploadId";
    public static final String PROPERTIES = "properties";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ElasticMoleculeDataService elasticMoleculeStorage;

    @Override
    public StreamTaskDescription deserialize(String json) {
        return restoreState(json).build(elasticMoleculeStorage);
    }

    @Override
    public String serialize(DescriptionState state) {
        return writeIntoString((Builder) state);
    }

    @Override
    public void setRequiredEntities(Object entityHolder) {
    }

    private Builder restoreState(String json) {
        var builder = new Builder();
        try {
            var map = objectMapper.readValue(json, Map.class);
            builder.setFileId(map.get(UPLOAD_ID).toString());
            Map<String, Property> properties = new HashMap<>();
            ((Map<?, ?>) map.get(PROPERTIES)).forEach((k, v) -> properties.put(k.toString(),
                    new Property(k.toString(), Property.PropertyType.valueOf(v.toString()))));
            builder.setProperties(properties);

        } catch (IOException e) {
            throw new MoleculeLibraryException("Cannot deserialize upload initiate task", e);
        }
        return builder;
    }

    public String writeIntoString(Builder builder) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    UPLOAD_ID, builder.fileId,
                    PROPERTIES, builder.properties));
        } catch (IOException e) {
            throw new MoleculeLibraryException("Cannot serialize upload initiate task", e);
        }
    }

    public static class Builder implements DescriptionState {

        private String fileId;
        private Map<String, Property> properties = new HashMap<>();

        public StreamTaskDescription build(ElasticMoleculeDataService elasticMoleculeStorage) {
            return new ResumableTaskDescription(
                    DataProvider.single(),
                    StreamTaskFunction.identity(),
                    new CreateLibraryResultAggregator(fileId, properties, elasticMoleculeStorage),
                    new CreateLibraryTaskSerDe(elasticMoleculeStorage),
                    USER,
                    UPLOAD
            ) {
                @Override
                public DescriptionState getState() {
                    return Builder.this;
                }
            }.setWeight(0).setSubscription(previous -> {

                var v = (TaskResult) previous;
                properties.putAll(v.aggregatedProperties());
            });
        }

        public Builder setFileId(String fileId) {
            this.fileId = fileId;
            return this;
        }

        public Builder setProperties(Map<String, Property> properties) {
            this.properties = properties;
            return this;
        }
    }
}
