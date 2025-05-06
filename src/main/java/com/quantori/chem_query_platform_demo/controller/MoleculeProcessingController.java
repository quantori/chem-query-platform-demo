package com.quantori.chem_query_platform_demo.controller;

import com.quantori.chem_query_platform_demo.event.SdfUploadEvent;
import com.quantori.chem_query_platform_demo.model.SearchResultResponse;
import com.quantori.chem_query_platform_demo.model.SearchStructure;
import com.quantori.chem_query_platform_demo.service.FileUploadService;
import com.quantori.chem_query_platform_demo.service.MoleculeSearchService;
import com.quantori.chem_query_platform_demo.utils.MolLoader;
import com.quantori.chem_query_platform_demo.utils.ZipMultipartUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MoleculeProcessingController {

    private final ApplicationEventPublisher publisher;
    private final FileUploadService fileUploadService;
    private final MoleculeSearchService moleculeSearchService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload .sdf file")
    public Mono<ResponseEntity<String>> uploadFile(
            @Parameter(
                    description = "The file to upload",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            ) @RequestParam("file") MultipartFile file) {
        return Mono.fromCallable(() -> {
                    File sdfFile = ZipMultipartUtils.extractSingleSdfFile(file);

                    String libraryName = fileUploadService.save(sdfFile.getName());
                    publisher.publishEvent(new SdfUploadEvent(sdfFile.getAbsolutePath(), libraryName));

                    log.info("Uploaded and processed file: {}, library: {}", sdfFile.getName(), libraryName);

                    return libraryName;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(lib -> ResponseEntity.accepted().body("File accepted for processing: " + lib))
                .onErrorResume(e -> {
                    log.error("Failed to upload file", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Upload failed: " + e.getMessage()));
                });
    }

    @PostMapping(value = "/search", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Search with uploaded .mol file and search parameters")
    public SearchResultResponse search(
            @Parameter(
                    description = ".mol file",
                    in = ParameterIn.DEFAULT,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart("file")
            MultipartFile file,

            @Parameter(
                    description = "JSON metadata file",
                    in = ParameterIn.DEFAULT,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart("metadata") SearchStructure searchStructure
    ) throws IOException {
        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("query.mol");
        String safeFilename = Paths.get(originalFilename).getFileName().toString();

        Path tempDir = Files.createTempDirectory("molsearch_");
        Path molFilePath = tempDir.resolve(safeFilename);
        file.transferTo(molFilePath.toFile());

        log.info("Received search file: {}", molFilePath);

        searchStructure.setQueryStructure(MolLoader.getQueryStructure(molFilePath));

        return moleculeSearchService.search(searchStructure);
    }
}