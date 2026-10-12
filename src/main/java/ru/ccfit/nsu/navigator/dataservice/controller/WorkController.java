package ru.ccfit.nsu.navigator.dataservice.controller;


import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.ccfit.nsu.navigator.dataservice.dto.DownloadFileResponse;
import ru.ccfit.nsu.navigator.dataservice.dto.WorkResponse;
import ru.ccfit.nsu.navigator.dataservice.service.WorkFilesService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;


@RestController
@RequestMapping("/api/works")
public class WorkController {
    private final WorkFilesService service;

    public WorkController(WorkFilesService service) {
        this.service = service;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Long>> uploadFiles(
            @RequestParam("surveyFile") MultipartFile surveyFile,
            @RequestParam("calculationFile") MultipartFile calculationFile,
            @RequestParam("workFile") MultipartFile workFile
    )throws IOException {
        Long id = service.saveFiles(surveyFile, calculationFile,workFile);
        return ResponseEntity.ok(Map.of("id", id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkResponse> getWorkInfo(@PathVariable Long id){
        WorkResponse response = service.getWorkInfo(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/files/{type}")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable Long id,
            @PathVariable String type
    ) {
        DownloadFileResponse file = service.getFile(id, type);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(file.filename(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file.content());
    }

    @GetMapping("/{id}/files")
    public ResponseEntity<byte[]> downloadWorkFiles(
            @PathVariable Long id
    ) throws IOException {
        byte[] archive = service.getFilesArchive(id);

        return ResponseEntity.ok().
                header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("work-" + id + ".zip")
                                .build()
                                .toString()
                ).contentType(MediaType.parseMediaType("application/zip"))
                .body(archive);
    }
}
