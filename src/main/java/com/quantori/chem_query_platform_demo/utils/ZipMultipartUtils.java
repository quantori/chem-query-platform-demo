package com.quantori.chem_query_platform_demo.utils;

import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@UtilityClass
public class ZipMultipartUtils {

    /**
     * Extracts a single .sdf file from the given ZIP MultipartFile.
     *
     * @param multipartFile the uploaded ZIP file
     * @return the extracted .sdf file
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the ZIP does not contain exactly one .sdf file
     */
    public static File extractSingleSdfFile(MultipartFile multipartFile) throws IOException {
        String originalFilename = Optional.ofNullable(multipartFile.getOriginalFilename()).orElse("upload.zip");

        Path tempDir = Files.createTempDirectory("unzip_");
        File zipFile = tempDir.resolve(originalFilename).toFile();

        multipartFile.transferTo(zipFile);

        File extractedSdf = null;

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".sdf")) {
                    if (extractedSdf != null) {
                        throw new IllegalArgumentException("ZIP archive contains more than one .sdf file");
                    }

                    extractedSdf = new File(tempDir.toFile(), Paths.get(entry.getName()).getFileName().toString());
                    try (FileOutputStream fos = new FileOutputStream(extractedSdf)) {
                        StreamUtils.copy(zis, fos);
                    }
                }
            }
        }

        if (extractedSdf == null) {
            throw new IllegalArgumentException("ZIP archive does not contain any .sdf file");
        }

        return extractedSdf;
    }
}
