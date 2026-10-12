package ru.ccfit.nsu.navigator.dataservice.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.ccfit.nsu.navigator.dataservice.dto.WorkResponse;
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

    @GetMapping("/{id}")
    public ResponseEntity<WorkResponse> getWorkInfo(@PathVariable Long id){
        WorkResponse response = service.getWorkInfo(id);
        return ResponseEntity.ok(response);
    }
}
