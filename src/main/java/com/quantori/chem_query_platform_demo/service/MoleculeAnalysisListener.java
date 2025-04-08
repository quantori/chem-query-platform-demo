package com.quantori.chem_query_platform_demo.service;

import com.quantori.chem_query_platform_demo.event.MoleculeAnalysisEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MoleculeAnalysisListener {
    private final MoleculeAnalysisService moleculeAnalysisService;

    @EventListener
    public void handle(MoleculeAnalysisEvent event) {
        moleculeAnalysisService
                .analyseMoleculeInFiles(event.filePath());
    }
}