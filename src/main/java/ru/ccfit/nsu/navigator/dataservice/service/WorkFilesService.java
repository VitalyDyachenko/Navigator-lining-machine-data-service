package ru.ccfit.nsu.navigator.dataservice.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.ccfit.nsu.navigator.dataservice.dto.DownloadFileResponse;
import ru.ccfit.nsu.navigator.dataservice.dto.WorkFilesResponse;
import ru.ccfit.nsu.navigator.dataservice.entity.*;
import ru.ccfit.nsu.navigator.dataservice.exception.InvalidFileTypeException;
import ru.ccfit.nsu.navigator.dataservice.exception.WorkNotFoundException;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.SurveyParser;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvSurvey;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvSurveyFlags;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiH.SrvTripId;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiH.SrvVpiHeader;
import ru.ccfit.nsu.navigator.dataservice.repository.*;
import ru.ccfit.nsu.navigator.dataservice.exception.SrvParsingException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
