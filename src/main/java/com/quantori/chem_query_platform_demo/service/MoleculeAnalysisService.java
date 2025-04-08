package com.quantori.chem_query_platform_demo.service;

import com.quantori.chem_query_platform_demo.exception.MoleculeAnalysisException;
import com.quantori.chem_query_platform_demo.serde.MoleculeAnalysisTaskSerDe;
import com.quantori.qdp.core.task.service.StreamTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MoleculeAnalysisService {

    private final StreamTaskService taskService;

    public void analyseMoleculeInFiles(String absolutePath) {

        var builder = new MoleculeAnalysisTaskSerDe.Builder().setAbsolutePath(absolutePath);
        try {
            taskService.processTask(builder.build(), null);
        } catch (Exception e) {
            throw new MoleculeAnalysisException(e.getMessage(), e);
        }
    }
}
