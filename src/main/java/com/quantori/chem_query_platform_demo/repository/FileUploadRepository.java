package com.quantori.chem_query_platform_demo.repository;

import com.quantori.chem_query_platform_demo.model.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileUploadRepository extends JpaRepository<FileUpload, UUID> {
}
