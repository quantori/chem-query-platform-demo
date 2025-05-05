package com.quantori.chem_query_platform_demo.stream_task_description.molecule_uploader;

import com.quantori.chem_query_platform_demo.serde.DataWrapper;
import com.quantori.chem_query_platform_demo.service.MoleculeStorageWriter;
import com.quantori.cqp.api.model.upload.Molecule;
import com.quantori.cqp.core.task.model.DataProvider;
import com.quantori.cqp.core.task.model.ResultAggregator;
import com.quantori.cqp.core.task.model.StreamTaskResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class MoleculeUploadResultAggregator implements ResultAggregator {
    private final MoleculeStorageWriter moleculeLibraryLoader;
    private final String libraryId;

    @Override
    public void consume(DataProvider.Data data) {
        List<ParsingRecord> parsingRecords =
                ((DataWrapper<List<ParsingRecord>>) data).data();
        indexData(parsingRecords);
    }

    private void indexData(List<ParsingRecord> parsingRecords) {
        List<Molecule> molecules = new ArrayList<>(parsingRecords.size());
        parsingRecords.forEach(parsingRecord -> {
            if (parsingRecord.molecule() != null) {
                molecules.add(parsingRecord.molecule());
            }
        });
        if (!molecules.isEmpty()) {
            moleculeLibraryLoader.add(molecules, libraryId);
        }
    }

    @Override
    public StreamTaskResult getResult() {
        return new UploadTaskResult("Success");
    }

    @Override
    public double getPercent() {
        return 0;
    }

    record UploadTaskResult(String result) implements StreamTaskResult{}
}
