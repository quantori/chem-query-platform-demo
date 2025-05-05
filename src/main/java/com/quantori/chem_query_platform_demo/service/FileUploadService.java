package com.quantori.chem_query_platform_demo.service;

import com.quantori.chem_query_platform_demo.model.FileUpload;
import com.quantori.chem_query_platform_demo.repository.FileUploadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileUploadService {
    private final FileUploadRepository fileUploadRepository;

    @Transactional
    public String save(final String fileName) {
        FileUpload fileUpload = fileUploadRepository.save(new FileUpload(fileName));
        return fileUpload.getId().toString();
    }
}
