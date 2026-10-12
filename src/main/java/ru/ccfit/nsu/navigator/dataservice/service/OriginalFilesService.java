package ru.ccfit.nsu.navigator.dataservice.service;


import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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
}
