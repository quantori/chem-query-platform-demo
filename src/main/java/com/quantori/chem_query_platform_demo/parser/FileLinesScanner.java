package com.quantori.chem_query_platform_demo.parser;

import com.quantori.chem_query_platform_demo.parser.exception.ParserException;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * A line-by-line scanner for reading text files, typically .sdf or .mol.
 * Implements Iterator and Closeable to allow safe usage in try-with-resources.
 */
@Slf4j
class FileLinesScanner implements Iterator<String>, Closeable {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final Scanner scanner;
    private final String filePath;
    private boolean closed = false;

    FileLinesScanner(String filePath) {
        this.filePath = filePath;
        try {
            this.scanner = new Scanner(new FileReader(filePath, CHARSET));
        } catch (IOException e) {
            throw new ParserException("Failed to open file for reading: " + filePath, e);
        }
    }

    @Override
    public boolean hasNext() {
        return !closed && scanner.hasNextLine();
    }

    /**
     * Returns the next line from the file.
     *
     * @return next line as String
     * @throws IllegalStateException if the scanner has been closed
     * @throws NoSuchElementException if there are no more lines
     * @throws ParserException if an I/O error occurs while reading
     */
    @Override
    public String next() {
        if (closed) {
            throw new IllegalStateException("Scanner is already closed");
        }

        if (!scanner.hasNextLine()) {
            throw new NoSuchElementException("No more lines to read from: " + filePath);
        }

        String line = scanner.nextLine();

        if (!scanner.hasNextLine() || scanner.ioException() != null) {
            close();
        }

        if (scanner.ioException() != null) {
            throw new ParserException("Error occurred while reading file: " + filePath, scanner.ioException());
        }

        return line;
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            try {
                scanner.close();
                log.debug("Scanner closed for file: {}", filePath);
            } catch (Exception e) {
                log.warn("Failed to close scanner for file: {}", filePath, e);
            }
        }
    }
}
