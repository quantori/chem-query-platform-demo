package com.quantori.chem_query_platform_demo.stream_task_description.molecule_uploader;

import com.quantori.cqp.api.model.upload.Molecule;

public record ParsingRecord(Molecule molecule, String errorMessage) {
}