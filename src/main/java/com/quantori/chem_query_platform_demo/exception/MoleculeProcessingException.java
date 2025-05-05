package com.quantori.chem_query_platform_demo.exception;

public class MoleculeProcessingException extends RuntimeException {

    public MoleculeProcessingException(String message, Exception e) {
        super(message, e);
    }
}
