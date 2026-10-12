package ru.ccfit.nsu.navigator.dataservice.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.ccfit.nsu.navigator.dataservice.service.OriginalFilesService;

import java.io.IOException;
import java.util.Map;


@RestController
@RequestMapping("/api/works")
public class WorkController {
    private final OriginalFilesService service;

    public WorkController(OriginalFilesService service) {
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
}
