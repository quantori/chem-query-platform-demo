package com.quantori.chem_query_platform_demo.controller;

import com.quantori.chem_query_platform_demo.event.MoleculeAnalysisEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MoleculeAnalysisController {

    private final ApplicationEventPublisher publisher;

    @PostMapping("/upload")
    public Mono<ResponseEntity<String>> uploadFile(@RequestParam("file") MultipartFile file) {
        return Mono.fromCallable(() -> {
                    String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("upload.sdf");
                    String safeName = Paths.get(filename).getFileName().toString();
                    String path = "/tmp/" + safeName;

                    file.transferTo(new File(path));
                    publisher.publishEvent(new MoleculeAnalysisEvent(path));
                    return path;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(path -> ResponseEntity.accepted().body("File accepted for processing: " + path))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Upload failed: " + e.getMessage()))
                );
    }
}
