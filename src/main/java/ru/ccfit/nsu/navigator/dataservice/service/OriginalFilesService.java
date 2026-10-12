package ru.ccfit.nsu.navigator.dataservice.service;


import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.ccfit.nsu.navigator.dataservice.dto.DownloadFileResponse;
import ru.ccfit.nsu.navigator.dataservice.dto.WorkResponse;
import ru.ccfit.nsu.navigator.dataservice.entity.OriginalFiles;
import ru.ccfit.nsu.navigator.dataservice.repository.OriginalFilesRepository;

import java.io.IOException;

@Service
public class OriginalFilesService {
    private final OriginalFilesRepository repository;

    public OriginalFilesService(OriginalFilesRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Long saveFiles(
            MultipartFile surveyFile,
            MultipartFile calculationFile,
            MultipartFile workFile
    )throws IOException{
        //TODO validation files
        OriginalFiles originalFiles = new OriginalFiles(
                surveyFile.getOriginalFilename(),
                surveyFile.getBytes(),
                calculationFile.getOriginalFilename(),
                calculationFile.getBytes(),
                workFile.getOriginalFilename(),
                workFile.getBytes()
        );

        return repository.save(originalFiles).getId();
    }

    @Transactional(readOnly = true)
    public WorkResponse getWorkInfo(Long id){
        OriginalFiles files = repository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Work not found"
                ));

        return new WorkResponse(
                files.getId(),
                files.getSurveyFilename(),
                files.getCalculationFilename(),
                files.getWorkFilename()
        );
    }

    @Transactional(readOnly = true)
    public DownloadFileResponse getFile(Long id, String type) {
        OriginalFiles files = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Work not found"
                ));

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
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unknown file type"
            );
        };
    }
}
