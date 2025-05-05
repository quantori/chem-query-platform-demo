package com.quantori.chem_query_platform_demo.stream_task_description.molecule_uploader;

import static com.quantori.chem_query_platform_demo.constants.TaskFlowConstants.UPLOAD;
import static com.quantori.chem_query_platform_demo.constants.TaskFlowConstants.USER;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantori.chem_query_platform_demo.stream_task_description.library_creator.SdfFileListDataProvider;
import com.quantori.chem_query_platform_demo.service.MoleculeLibraryException;
import com.quantori.chem_query_platform_demo.service.MoleculeStorageWriter;
import com.quantori.chem_query_platform_demo.indigo.IndigoStructureDecoder;
import com.quantori.chem_query_platform_demo.indigo.IndigoMoleculeDecoder;
import com.quantori.cqp.core.task.model.DescriptionState;
import com.quantori.cqp.core.task.model.ResumableTaskDescription;
import com.quantori.cqp.core.task.model.StreamTaskDescription;
import com.quantori.cqp.core.task.model.TaskDescriptionSerDe;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
public class MoleculeUploadSerDe implements TaskDescriptionSerDe {
    public static final String ABSOLUTE_PATH = "absolutePath";
    public static final String FILE_ID = "fileId";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MoleculeStorageWriter moleculeStorage;
    private final IndigoMoleculeDecoder indigoMoleculeDecoder;
    private final IndigoStructureDecoder indigoStructureDecoder;

    @Override
    public StreamTaskDescription deserialize(String json) {
        final var builder = restoreState(json);
        return builder.build(moleculeStorage, indigoStructureDecoder, indigoMoleculeDecoder);
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
            builder.setAbsolutePath(map.get(ABSOLUTE_PATH).toString())
                    .setFileId(map.get(FILE_ID).toString());

        } catch (IOException e) {
            throw new MoleculeLibraryException("Cannot deserialize the sdf load task", e);
        }
        return builder;
    }

    public String writeIntoString(Builder builder) {
        try {
            return objectMapper.writeValueAsString(
                    Map.of(
                            ABSOLUTE_PATH, builder.absolutePath,
                            FILE_ID, builder.fileId));
        } catch (IOException e) {
            throw new MoleculeLibraryException("Cannot serialize the sdf load task", e);
        }
    }

    public static class Builder implements DescriptionState {
        private String absolutePath;
        private String fileId;

        public StreamTaskDescription build(MoleculeStorageWriter moleculeStorageWriter, IndigoStructureDecoder indigoStructureDecoder,
                                           IndigoMoleculeDecoder indigoMoleculeDecoder) {

            return new ResumableTaskDescription(
                    new SdfFileListDataProvider(absolutePath),
                    new MoleculeLoaderStreamTaskFunction(indigoStructureDecoder, indigoMoleculeDecoder),
                    new MoleculeUploadResultAggregator(moleculeStorageWriter, fileId),
                    new MoleculeUploadSerDe(moleculeStorageWriter, indigoMoleculeDecoder, indigoStructureDecoder),
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
        public Builder setFileId(String fileId) {
            this.fileId = fileId;
            return this;
        }
    }
}
