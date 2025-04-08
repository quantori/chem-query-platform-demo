package com.quantori.chem_query_platform_demo.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantori.chem_query_platform_demo.exception.MoleculeAnalysisException;
import com.quantori.chem_query_platform_demo.result_aggregator.MoleculeResultAggregator;
import com.quantori.chem_query_platform_demo.parser.SDFParser;
import com.quantori.qdp.core.task.model.DescriptionState;
import com.quantori.qdp.core.task.model.StreamTaskDescription;
import com.quantori.qdp.core.task.model.TaskDescriptionSerDe;
import com.quantori.qdp.core.task.model.ResumableTaskDescription;
import com.quantori.qdp.core.task.model.StreamTaskFunction;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Map;

/**
 * {@code FileParserTaskSerDe} is an implementation of {@link TaskDescriptionSerDe}
 * used in an Akka Stream pipeline to serialize and deserialize task descriptions
 * that configure molecule parsing operations (e.g. reading SDF files).
 * <p>
 * This class delegates task configuration to its static nested {@link Builder} class,
 * which encapsulates the logic for constructing a {@link StreamTaskDescription}—
 * defining the full stream behavior: data source, transformation, aggregation, and serialization.
 * <p>
 */
@NoArgsConstructor
public class MoleculeAnalysisTaskSerDe implements TaskDescriptionSerDe {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Deserializes JSON into a {@link Builder}, which builds the actual task description.
     *
     * @param json JSON string containing the serialized task state
     * @return a fully built {@link StreamTaskDescription}
     * @throws RuntimeException if JSON parsing fails
     */
    @Override
    public StreamTaskDescription deserialize(String json) {
        return restoreState(json).build();
    }

    /**
     * Serializes the current task state (from {@link Builder}) into a JSON string.
     *
     * @param descriptionState an instance of {@link Builder}
     * @return serialized JSON representation
     * @throws RuntimeException if serialization fails
     */
    @Override
    public String serialize(DescriptionState descriptionState) {
        return writeIntoString((Builder) descriptionState);
    }

    /**
     * Stateless method; does nothing as this SerDe has no required external dependencies.
     */
    @Override
    public void setRequiredEntities(Object entityHolder) {
    }

    private Builder restoreState(String json) {
        final var builder = new Builder();
        try {
            var map = objectMapper.readValue(json, Map.class);
            builder
                    .setAbsolutePath(map.get("absolutePath").toString());
        } catch (IOException exception) {
            throw new MoleculeAnalysisException("Cannot deserialize quick count molecules task", exception);
        }
        return builder;
    }

    public String writeIntoString(Builder builder) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "absolutePath", builder.absolutePath
            ));
        } catch (IOException exception) {
            throw new MoleculeAnalysisException("Cannot serialize quick count molecules task", exception);
        }
    }

    /**
     * The {@code Builder} class acts as the core configuration unit of the task.
     * <p>
     * It defines the complete pipeline logic: parsing SDF molecules, wrapping them as data,
     * applying identity transformation, aggregating results, and enabling task resumption.
     * <p>
     * When {@link #build()} is called, it returns a {@link ResumableTaskDescription},
     * ready to be executed in the Akka-based {@link com.quantori.qdp.core.task.service.StreamTaskService}.
     */
    public static class Builder implements DescriptionState {
        private String absolutePath;

        public StreamTaskDescription build() {
            return new ResumableTaskDescription(
                    () -> new TransformIterator<>(new SDFParser(absolutePath),
                            DataWrapper::new),
                    StreamTaskFunction.identity(),
                    new MoleculeResultAggregator(),
                    new MoleculeAnalysisTaskSerDe(),
                    "user",
                    "upload") {
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
