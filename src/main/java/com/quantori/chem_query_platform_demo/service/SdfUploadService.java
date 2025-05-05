package com.quantori.chem_query_platform_demo.service;

import static com.quantori.chem_query_platform_demo.constants.TaskFlowConstants.UPLOAD;
import static com.quantori.chem_query_platform_demo.constants.TaskFlowConstants.USER;

import com.quantori.chem_query_platform_demo.event.SdfUploadEvent;
import com.quantori.chem_query_platform_demo.exception.MoleculeProcessingException;
import com.quantori.chem_query_platform_demo.stream_task_description.molecule_uploader.MoleculeUploadSerDe;
import com.quantori.chem_query_platform_demo.stream_task_description.library_creator.CreateLibraryTaskSerDe;
import com.quantori.chem_query_platform_demo.indigo.IndigoStructureDecoder;
import com.quantori.chem_query_platform_demo.indigo.IndigoMoleculeDecoder;
import com.quantori.chem_query_platform_demo.stream_task_description.properties_validator.PropertiesValidatorTaskSerDe;
import com.quantori.cqp.core.task.model.StreamTaskDescription;
import com.quantori.cqp.core.task.service.StreamTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SdfUploadService {

    private final StreamTaskService taskService;
    private final IndigoStructureDecoder indigoStructureDecoder;
    private final ElasticMoleculeDataService elasticMoleculeStorage;
    private final MoleculeStorageWriter moleculeLibraryLoader;
    private final IndigoMoleculeDecoder indigoMoleculeDecoder;

    @EventListener
    public void buildUploadPipeline(SdfUploadEvent event) {
        try {
            List<StreamTaskDescription> taskFlow = buildTaskFlow(event);
            taskService.processTaskFlowAsTask(taskFlow, UPLOAD, unused -> {}, USER, 1, 1);
        } catch (Exception e) {
            throw new MoleculeProcessingException("Failed to process uploaded molecules", e);
        }
    }

    private List<StreamTaskDescription> buildTaskFlow(SdfUploadEvent event) {
        var validationBuilder = new PropertiesValidatorTaskSerDe.Builder()
                .setAbsolutePath(event.filePath());

        var createLibraryBuilder = new CreateLibraryTaskSerDe.Builder()
                .setFileId(event.libraryId());

        var uploadBuilder = new MoleculeUploadSerDe.Builder()
                .setAbsolutePath(event.filePath())
                .setFileId(event.libraryId());

        return List.of(
                validationBuilder.build(indigoStructureDecoder),
                createLibraryBuilder.build(elasticMoleculeStorage),
                uploadBuilder.build(moleculeLibraryLoader, indigoStructureDecoder, indigoMoleculeDecoder)
        );
    }
}
