package com.quantori.chem_query_platform_demo.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ZipMultipartUtilsTest {

    private byte[] createZipWithEntries(String... fileNames) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (String fileName : fileNames) {
                zos.putNextEntry(new ZipEntry(fileName));
                zos.write(("dummy content for " + fileName).getBytes());
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    @Test
    void testExtractSingleSdfFile_Success() throws IOException {
        byte[] zipContent = createZipWithEntries("data.sdf");
        MockMultipartFile multipartFile = new MockMultipartFile("file", "archive.zip", "application/zip", zipContent);

        File result = ZipMultipartUtils.extractSingleSdfFile(multipartFile);

        assertNotNull(result);
        assertTrue(result.getName().endsWith(".sdf"));
        assertTrue(result.exists());
    }

    @Test
    void testExtractSingleSdfFile_ThrowsIfMultipleSdf() throws IOException {
        byte[] zipContent = createZipWithEntries("file1.sdf", "file2.sdf");
        MockMultipartFile multipartFile = new MockMultipartFile("file", "archive.zip", "application/zip", zipContent);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ZipMultipartUtils.extractSingleSdfFile(multipartFile));

        assertEquals("ZIP archive contains more than one .sdf file", ex.getMessage());
    }

    @Test
    void testExtractSingleSdfFile_ThrowsIfNoSdf() throws IOException {
        byte[] zipContent = createZipWithEntries("notes.txt", "data.csv");
        MockMultipartFile multipartFile = new MockMultipartFile("file", "archive.zip", "application/zip", zipContent);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ZipMultipartUtils.extractSingleSdfFile(multipartFile));

        assertEquals("ZIP archive does not contain any .sdf file", ex.getMessage());
    }
}
