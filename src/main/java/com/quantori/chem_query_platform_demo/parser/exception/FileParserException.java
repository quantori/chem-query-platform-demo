package com.quantori.chem_query_platform_demo.parser.exception;

public class FileParserException extends RuntimeException{
    public FileParserException(String message, Exception e) {
        super(message, e);
    }

    public FileParserException(String message) {
        super(message);
    }
}
