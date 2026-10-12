package ru.ccfit.nsu.navigator.dataservice.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.ccfit.nsu.navigator.dataservice.dto.DownloadFileResponse;
import ru.ccfit.nsu.navigator.dataservice.dto.ErrorResponse;
import ru.ccfit.nsu.navigator.dataservice.dto.UploadResponse;
import ru.ccfit.nsu.navigator.dataservice.dto.WorkResponse;
import ru.ccfit.nsu.navigator.dataservice.service.WorkFilesService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;


@RestController
@RequestMapping("/api/works")
@Tag(name = "Work Files", description = "Операции для загрузки и скачивания файлов работ")
public class WorkController {
    private final WorkFilesService service;

    public WorkController(WorkFilesService service) {
        this.service = service;
    }

    @Operation(
            summary = "Загрузить файлы работы",
            description = "Загружает файлы работы в хранилище файлов."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Файлы успешно сохранены"),
//            @ApiResponse(
//                    responseCode = "400",
//                    description = "Один из файлов отсутствует или не передан",
//                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
//            ),
//            @ApiResponse(
//                    responseCode = "500",
//                    description = "Ошибка чтения или сохранения файла",
//                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
//            )
    })
    @PostMapping(consumes = "multipart/form-data", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UploadResponse> uploadFiles(
            @Parameter(description = "Файл съёмки", required = true)
            @RequestParam("surveyFile")
            MultipartFile surveyFile,
            @Parameter(description = "Файл проекта", required = true)
            @RequestParam("calculationFile")
            MultipartFile calculationFile,
            @Parameter(description = "Файл выправки", required = true)
            @RequestParam("workFile")
            MultipartFile workFile
    )throws IOException {
        Long id = service.saveFiles(surveyFile, calculationFile,workFile);
        return ResponseEntity.ok(new UploadResponse(id));
    }

    @Operation(
            summary = "Получить информацию о файлах работы"
    )
    @GetMapping(value  = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkResponse> getWorkInfo(@PathVariable Long id){
        WorkResponse response = service.getWorkInfo(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Скачать 1 файл работы")
    @ApiResponse(
            responseCode = "200",
            description = "Файл",
            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    )
    @GetMapping("/{id}/files/{type}")
    public ResponseEntity<byte[]> downloadFile(
            @Parameter(description = "ID работы")
            @PathVariable
            Long id,
            @Parameter(description = "Тип файла", schema = @Schema(allowableValues = {"survey", "calculation", "work"}))
            @PathVariable
            String type
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

    @Operation(summary = "Скачать все файлы работы")
    @ApiResponse(
            responseCode = "200",
            description = "ZIP-архив",
            content = @Content(mediaType = "application/zip")
    )
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
