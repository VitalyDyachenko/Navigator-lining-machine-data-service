package ru.ccfit.nsu.navigator.dataservice.service;


import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.ccfit.nsu.navigator.dataservice.dto.DownloadFileResponse;
import ru.ccfit.nsu.navigator.dataservice.dto.WorkFilesResponse;
import ru.ccfit.nsu.navigator.dataservice.entity.WorkFiles;
import ru.ccfit.nsu.navigator.dataservice.exception.InvalidFileTypeException;
import ru.ccfit.nsu.navigator.dataservice.exception.WorkNotFoundException;
import ru.ccfit.nsu.navigator.dataservice.repository.WorkFilesRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class WorkFilesService {
    private final WorkFilesRepository repository;

    public WorkFilesService(WorkFilesRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Long saveFiles(
            MultipartFile surveyFile,
            MultipartFile calculationFile,
            MultipartFile workFile
    ) throws IOException {
        //TODO validation files
        WorkFiles workFiles = new WorkFiles(
                surveyFile.getOriginalFilename(),
                surveyFile.getBytes(),
                calculationFile.getOriginalFilename(),
                calculationFile.getBytes(),
                workFile.getOriginalFilename(),
                workFile.getBytes()
        );

        return repository.save(workFiles).getId();
    }

    @Transactional(readOnly = true)
    public WorkFilesResponse getWorkFilesInfo(Long id) {
        WorkFiles files = repository.findById(id)
                .orElseThrow(()-> new WorkNotFoundException(id));

        return new WorkFilesResponse(
                files.getId(),
                files.getSurveyFilename(),
                files.getCalculationFilename(),
                files.getWorkFilename()
        );
    }

    @Transactional(readOnly = true)
    public DownloadFileResponse getFile(Long id, String type) {
        WorkFiles files = repository.findById(id)
                .orElseThrow(() -> new WorkNotFoundException(id));

        return switch (type) {
            case "survey" -> new DownloadFileResponse(
                    files.getSurveyFilename(),
                    files.getSurveyFile()
            );
            case "calculation" -> new DownloadFileResponse(
                    files.getCalculationFilename(),
                    files.getCalculationFile()
            );
            case "work" -> new DownloadFileResponse(
                    files.getWorkFilename(),
                    files.getWorkFile()
            );
            default -> throw new InvalidFileTypeException(type);
        };
    }

    @Transactional(readOnly = true)
    public byte[] getFilesArchive(Long id) throws IOException {
        WorkFiles files = repository.findById(id)
                .orElseThrow(() -> new WorkNotFoundException(id));

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            addToZip(zip, files.getSurveyFilename(), files.getSurveyFile());
            addToZip(zip, files.getCalculationFilename(), files.getCalculationFile());
            addToZip(zip, files.getWorkFilename(), files.getWorkFile());
        }

        return output.toByteArray();
    }

    private void addToZip(
            ZipOutputStream zip,
            String filename,
            byte[] content
    ) throws IOException {
        String safeName = filename.replace('\\', '/');
        safeName = safeName.substring(safeName.lastIndexOf('/') + 1);

        zip.putNextEntry(new ZipEntry(safeName));
        zip.write(content);
        zip.closeEntry();
    }
}
