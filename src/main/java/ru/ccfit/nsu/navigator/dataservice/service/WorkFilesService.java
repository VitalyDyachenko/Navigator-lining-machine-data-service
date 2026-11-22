package ru.ccfit.nsu.navigator.dataservice.service;


import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.ccfit.nsu.navigator.dataservice.dto.DownloadFileResponse;
import ru.ccfit.nsu.navigator.dataservice.dto.WorkFilesResponse;
import ru.ccfit.nsu.navigator.dataservice.entity.Work;
import ru.ccfit.nsu.navigator.dataservice.entity.WorkFiles;
import ru.ccfit.nsu.navigator.dataservice.exception.InvalidFileTypeException;
import ru.ccfit.nsu.navigator.dataservice.exception.WorkNotFoundException;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.SurveyParser;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvSurvey;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track.SrvTrack;
import ru.ccfit.nsu.navigator.dataservice.repository.WorkFilesRepository;
import ru.ccfit.nsu.navigator.dataservice.repository.WorkRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.Machine;
import ru.ccfit.nsu.navigator.dataservice.entity.MachineParams;

import ru.ccfit.nsu.navigator.dataservice.repository.MachineRepository;
import ru.ccfit.nsu.navigator.dataservice.repository.MachineParamsRepository;

import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvMachine;
import ru.ccfit.nsu.navigator.dataservice.exception.SrvParsingException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class WorkFilesService {
    private final WorkFilesRepository filesRepository;
    private final WorkRepository workRepository;
    private final MachineRepository machineRepository;
    private final MachineParamsRepository machineParamsRepository;

    public WorkFilesService(
            WorkFilesRepository filesRepository,
            WorkRepository workRepository,
            MachineRepository machineRepository,
            MachineParamsRepository machineParamsRepository
    ) {
        this.filesRepository = filesRepository;
        this.workRepository = workRepository;
        this.machineRepository = machineRepository;
        this.machineParamsRepository = machineParamsRepository;
    }



    @Transactional
    public Long saveFiles(MultipartFile surveyFile, MultipartFile calculationFile,
                          MultipartFile workFile) throws IOException {
        byte[] surveyBytes = surveyFile.getBytes();
        byte[] calculationBytes = calculationFile.getBytes();
        byte[] workBytes = workFile.getBytes();

        SrvSurvey survey = new SurveyParser().parse(surveyBytes);
        SrvMachine sourceMachine = survey.header().machine();

        Machine machine = getOrCreateMachine(sourceMachine);
        saveMachineParams(machine, sourceMachine);

        Work work = saveWork(survey.header(), machine);

        WorkFiles files = new WorkFiles(
                work.getId(),
                surveyFile.getOriginalFilename(), surveyBytes,
                calculationFile.getOriginalFilename(), calculationBytes,
                workFile.getOriginalFilename(), workBytes
        );
        filesRepository.save(files);

        return work.getId();
    }

    private Machine getOrCreateMachine(SrvMachine source) {
        if (source.machineType() < 0 || source.machineType() > 4) {
            throw new SrvParsingException("Unsupported machine type: " + source.machineType());
        }

        short typeId = (short) source.machineType();

        return machineRepository.findFirstByNumberAndNameAndTypeId(
                source.number(), source.name(), typeId
        ).orElseGet(() -> machineRepository.save(
                new Machine(source.number(), source.name(), typeId)
        ));
    }

    private void saveMachineParams(Machine machine, SrvMachine source) {
        MachineParams params = new MachineParams(
                machine,
                source.continuousCorrection(),
                source.profileEnabled(),
                source.planeSatellite(),
                source.tampingSleepers(),
                source.alignmentArms(),
                source.levelingArms(),
                source.trolleyArms(),
                source.gaugeWidth()
        );

        boolean exists = machineParamsRepository.findAllByMachine_Id(machine.getId())
                .stream()
                .anyMatch(existing -> existing.hasSameSettings(params));

        if (!exists) {
            machineParamsRepository.save(params);
        }
    }

    private Work saveWork(SrvHeader header, Machine machine) {
        SrvTrack track = header.track();
        Work work = Work.createPending();

        work.setMachine(machine);
        work.setTrackInfo(track.trackNumber(), track.administrativeCode());
        work.setTrackRange(
                track.begin().km(), track.begin().meters(), track.begin().millimeters(),
                track.end().km(), track.end().meters(), track.end().millimeters()
        );
        work.setMeasurementInfo(header.stepSizeMm(), header.stepCount());

        return workRepository.save(work);
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
