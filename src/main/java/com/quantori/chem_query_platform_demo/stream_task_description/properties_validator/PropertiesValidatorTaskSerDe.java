package com.quantori.chem_query_platform_demo.stream_task_description.properties_validator;

import static com.quantori.chem_query_platform_demo.constants.TaskFlowConstants.UPLOAD;
import static com.quantori.chem_query_platform_demo.constants.TaskFlowConstants.USER;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantori.chem_query_platform_demo.service.MoleculeLibraryException;
import com.quantori.chem_query_platform_demo.indigo.IndigoStructureDecoder;
import com.quantori.cqp.core.task.model.DescriptionState;
import com.quantori.cqp.core.task.model.ResumableTaskDescription;
import com.quantori.cqp.core.task.model.StreamTaskDescription;
import com.quantori.cqp.core.task.model.TaskDescriptionSerDe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class PropertiesValidatorTaskSerDe implements TaskDescriptionSerDe {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final IndigoStructureDecoder moleculeContext;

    @Override
    public StreamTaskDescription deserialize(String json) {
        return restoreState(json).build(moleculeContext);
    }

    @Override
    public String serialize(DescriptionState state) {
        return writeIntoString((Builder) state);
    }

    @Override
    public void setRequiredEntities(Object ignore) {
    }

    private Builder restoreState(String json) {
        final var builder = new Builder();
        try {
            var map = objectMapper.readValue(json, Map.class);
            log.info("Restored state of builder properties: {}", map);
            builder.setAbsolutePath(map.get("absolutePath").toString());
        } catch (IOException | NumberFormatException | ClassCastException e) {
            throw new MoleculeLibraryException("Cannot deserialize validation task", e);
        }
        return builder;
    }

    private String writeIntoString(Builder builder) {
        Map<String, Object> builderProperties = Map.of("absolutePath", builder.absolutePath);
        log.info("Builder properties: {}", builderProperties);
        try {
            return objectMapper.writeValueAsString(builderProperties);
        } catch (IOException e) {
            throw new MoleculeLibraryException("Cannot serialize validation task", e);
        }
    }

    public static class Builder implements DescriptionState {
        private String absolutePath;

        public StreamTaskDescription build(IndigoStructureDecoder moleculeContext) {
            return new ResumableTaskDescription(
                    new SdfFileDataProvider(absolutePath),
                    new MoleculePropertyStreamTaskFunction(moleculeContext),
                    new MoleculePropertiesAggregator(),
                    new PropertiesValidatorTaskSerDe(moleculeContext),
                    USER,
                    UPLOAD) {
                @Override
                public DescriptionState getState() {
                    return Builder.this;
                }
            }.setWeight(0);
        }

        public Builder setAbsolutePath(String absolutePath) {
            this.absolutePath = absolutePath;
            return this;
        }
    }
}
