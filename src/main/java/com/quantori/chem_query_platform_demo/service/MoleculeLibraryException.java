package com.quantori.chem_query_platform_demo.service;

public class MoleculeLibraryException extends RuntimeException {
    public MoleculeLibraryException(String message, Exception e) {
        super(message, e);
    }

    public MoleculeLibraryException(String message){
        super(message);
    }
}
