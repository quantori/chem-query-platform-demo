package com.quantori.chem_query_platform_demo.service;

import static com.quantori.chem_query_platform_demo.constants.TaskFlowConstants.UPLOAD;
import static com.quantori.chem_query_platform_demo.constants.TaskFlowConstants.USER;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quantori.chem_query_platform_demo.event.SdfUploadEvent;
import com.quantori.chem_query_platform_demo.exception.MoleculeProcessingException;
import com.quantori.chem_query_platform_demo.indigo.IndigoMoleculeDecoder;
import com.quantori.chem_query_platform_demo.indigo.IndigoStructureDecoder;
import com.quantori.cqp.core.task.service.StreamTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SdfUploadServiceTest {

    private StreamTaskService taskService;
    private SdfUploadService sdfUploadService;

    @BeforeEach
    void setUp() {
        taskService = mock(StreamTaskService.class);
        IndigoStructureDecoder indigoStructureDecoder = mock(IndigoStructureDecoder.class);
        ElasticMoleculeDataService elasticMoleculeStorage = mock(ElasticMoleculeDataService.class);
        MoleculeStorageWriter moleculeLibraryLoader = mock(MoleculeStorageWriter.class);
        IndigoMoleculeDecoder indigoMoleculeDecoder = mock(IndigoMoleculeDecoder.class);

        sdfUploadService = new SdfUploadService(
                taskService,
                indigoStructureDecoder,
                elasticMoleculeStorage,
                moleculeLibraryLoader,
                indigoMoleculeDecoder
        );
    }

    @Test
    void testBuildUploadPipeline_Success() {
        SdfUploadEvent event = new SdfUploadEvent("file.sdf", "lib123");

        sdfUploadService.buildUploadPipeline(event);

        verify(taskService).processTaskFlowAsTask(
                anyList(), eq(UPLOAD), any(), eq(USER), eq(1), eq(1)
        );
    }

    @Test
    void testBuildUploadPipeline_ThrowsMoleculeProcessingException() {
        SdfUploadEvent event = new SdfUploadEvent("bad-path.sdf", "lib-bad");

        when(taskService.processTaskFlowAsTask(anyList(), anyString(), any(), anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Simulated Failure"));
        MoleculeProcessingException ex = assertThrows(MoleculeProcessingException.class,
                () -> sdfUploadService.buildUploadPipeline(event));
        assertTrue(ex.getMessage().contains("Failed to process uploaded molecules"));
    }
}
