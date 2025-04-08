package com.quantori.chem_query_platform_demo.exception;

public class MoleculeAnalysisException extends RuntimeException {

    public MoleculeAnalysisException(String message, Exception e) {
        super(message, e);
    }

    public MoleculeAnalysisException(String message) {
        super(message);
    }
}
