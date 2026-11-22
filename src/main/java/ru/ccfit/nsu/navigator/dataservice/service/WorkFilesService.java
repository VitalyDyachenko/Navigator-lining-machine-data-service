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
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvSurvey;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvSurveyFlags;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track.SrvTrack;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiH.SrvTripId;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiH.SrvVpiHeader;
import ru.ccfit.nsu.navigator.dataservice.repository.*;

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
    private final SurveyRepository surveyRepository;

    public WorkFilesService(
            WorkFilesRepository filesRepository,
            WorkRepository workRepository,
            MachineRepository machineRepository,
            MachineParamsRepository machineParamsRepository,
            SurveyRepository surveyRepository
    ) {
        this.filesRepository = filesRepository;
        this.workRepository = workRepository;
        this.machineRepository = machineRepository;
        this.machineParamsRepository = machineParamsRepository;
        this.surveyRepository = surveyRepository;
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
        MachineParams machineParams = getOrCreateMachineParams(machine, sourceMachine);

        Work work = saveWork(survey.header(), machine);
        saveSurvey(work, machineParams, survey.header());

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


    private MachineParams getOrCreateMachineParams(Machine machine, SrvMachine source) {
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

        return machineParamsRepository.findAllByMachine_Id(machine.getId())
                .stream()
                .filter(existing -> existing.hasSameSettings(params))
                .findFirst()
                .orElseGet(() -> machineParamsRepository.save(params));
    }

    private void saveSurvey(Work work, MachineParams machineParams, SrvHeader header) {
        Survey survey = new Survey(
                work.getId(),
                machineParams,
                (short) header.version(),
                header.surveyTime(),
                header.channelMask()
        );

        setSurveyFlags(survey, header.flags());
        setSurveyTrip(survey, header.vpi());

        surveyRepository.save(survey);
    }

    private void setSurveyFlags(Survey survey, SrvSurveyFlags flags) {
        int press = flags.press();

        if (press == 2) {
            throw new SrvParsingException("Unsupported press value: " + press);
        }

        survey.setFlags(
                flags.againstPicketage(),
                flags.movingBackward(),
                press == 0 || press == 3,
                press == 1 || press == 3,
                flags.shortChord(),
                (short) flags.intertrackSpace(),
                flags.markerAtFront(),
                flags.dlsEnabled()
        );
    }

    private void setSurveyTrip(Survey survey, SrvVpiHeader vpi) {
        SrvTripId trip = vpi.tripId();

        survey.setTrip(
                vpi.startStep(),
                trip.tripNumber(),
                (short) trip.wagonNumber(),
                (short) trip.year(),
                (short) trip.month(),
                (short) trip.railwayCode(),
                (short) trip.checkCode(),
                trip.checked()
        );
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
