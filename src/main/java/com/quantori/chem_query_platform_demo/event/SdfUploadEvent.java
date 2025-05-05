package com.quantori.chem_query_platform_demo.event;

public record SdfUploadEvent(String filePath, String libraryId) {
}