package ru.ccfit.nsu.navigator.dataservice.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.ccfit.nsu.navigator.dataservice.dto.DownloadFileResponse;
import ru.ccfit.nsu.navigator.dataservice.dto.WorkFilesResponse;
import ru.ccfit.nsu.navigator.dataservice.entity.work.WorkFiles;
import ru.ccfit.nsu.navigator.dataservice.exception.InvalidFileTypeException;
import ru.ccfit.nsu.navigator.dataservice.exception.WorkNotFoundException;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.SurveyParser;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvSurvey;
import ru.ccfit.nsu.navigator.dataservice.repository.work.WorkFilesRepository;
import ru.ccfit.nsu.navigator.dataservice.service.persistence.SurveyPersistenceService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class WorkFilesService {
    private final WorkFilesRepository filesRepository;
    private final SurveyPersistenceService surveyPersistenceService;

    public WorkFilesService(WorkFilesRepository filesRepository,
                            SurveyPersistenceService surveyPersistenceService)
    {
        this.filesRepository = filesRepository;
        this.surveyPersistenceService = surveyPersistenceService;
    }

    @Transactional
    public Long saveFiles(MultipartFile surveyFile, MultipartFile calculationFile,
                          MultipartFile workFile) throws IOException
    {
        byte[] surveyBytes = surveyFile.getBytes();
        byte[] calculationBytes = calculationFile.getBytes();
        byte[] workBytes = workFile.getBytes();

        SrvSurvey survey = new SurveyParser().parse(surveyBytes);
        Long workId = surveyPersistenceService.save(survey);

        WorkFiles files = new WorkFiles(
                workId,
                surveyFile.getOriginalFilename(), surveyBytes,
                calculationFile.getOriginalFilename(), calculationBytes,
                workFile.getOriginalFilename(), workBytes
        );

        filesRepository.save(files);
        return workId;
    }

    @Transactional(readOnly = true)
    public WorkFilesResponse getWorkFilesInfo(Long id) {
        return filesRepository.findFilesInfoById(id)
                .orElseThrow(() -> new WorkNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public DownloadFileResponse getFile(Long id, String type) {
        WorkFiles files = filesRepository.findById(id)
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
        WorkFiles files = filesRepository.findById(id)
                .orElseThrow(() -> new WorkNotFoundException(id));

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            Set<String> names = new HashSet<>();
            addToZip(zip, names, "survey", files.getSurveyFilename(), files.getSurveyFile());
            addToZip(zip, names, "calculation", files.getCalculationFilename(), files.getCalculationFile());
            addToZip(zip, names, "work", files.getWorkFilename(), files.getWorkFile());
        }

        return output.toByteArray();
    }

    private void addToZip(
            ZipOutputStream zip,
            Set<String> names,
            String type,
            String filename,
            byte[] content
    ) throws IOException {
        String safeName = filename.replace('\\', '/');
        safeName = safeName.substring(safeName.lastIndexOf('/') + 1);
        if (safeName.isBlank() || safeName.equals(".") || safeName.equals("..")) {
            safeName = type + ".bin";
        }
        String entryName = safeName;
        int suffix = 2;
        if (names.contains(entryName)) entryName = type + "-" + safeName;
        while (!names.add(entryName)) {
            entryName = type + "-" + suffix++ + "-" + safeName;
        }

        zip.putNextEntry(new ZipEntry(entryName));
        zip.write(content);
        zip.closeEntry();
    }
}
