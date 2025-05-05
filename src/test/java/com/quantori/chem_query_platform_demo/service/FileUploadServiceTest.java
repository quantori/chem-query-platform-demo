package com.quantori.chem_query_platform_demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quantori.chem_query_platform_demo.model.FileUpload;
import com.quantori.chem_query_platform_demo.repository.FileUploadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class FileUploadServiceTest {

    private FileUploadRepository fileUploadRepository;
    private FileUploadService fileUploadService;

    @BeforeEach
    void setUp() {
        fileUploadRepository = mock(FileUploadRepository.class);
        fileUploadService = new FileUploadService(fileUploadRepository);
    }

    @Test
    void testSave_ReturnsId() {
        String fileName = "example.txt";
        UUID id = UUID.randomUUID();
        FileUpload mockUpload = new FileUpload(fileName);
        mockUpload.setId(id);

        when(fileUploadRepository.save(any(FileUpload.class))).thenReturn(mockUpload);

        String returnedId = fileUploadService.save(fileName);

        assertEquals(id.toString(), returnedId);
        verify(fileUploadRepository).save(any(FileUpload.class));
    }
}
