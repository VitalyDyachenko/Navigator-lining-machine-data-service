
package ru.ccfit.nsu.navigator.dataservice.service;

import org.springframework.stereotype.Service;
import ru.ccfit.nsu.navigator.dataservice.entity.*;
import ru.ccfit.nsu.navigator.dataservice.exception.SrvParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.*;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track.SrvTrack;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiH.*;
import ru.ccfit.nsu.navigator.dataservice.repository.*;

@Service
public class SurveyPersistenceService {

    private final MachineRepository machineRepository;
    private final MachineParamsRepository machineParamsRepository;
    private final WorkRepository workRepository;
    private final SurveyRepository surveyRepository;
    private final SurveyVpiPersistenceService vpiPersistenceService;
    private final SurveyDataPersistenceService dataPersistenceService;

    public SurveyPersistenceService(
            MachineRepository machineRepository,
            MachineParamsRepository machineParamsRepository,
            WorkRepository workRepository,
            SurveyRepository surveyRepository,
            SurveyVpiPersistenceService vpiPersistenceService,
            SurveyDataPersistenceService dataPersistenceService
    ) {
        this.machineRepository = machineRepository;
        this.machineParamsRepository = machineParamsRepository;
        this.workRepository = workRepository;
        this.surveyRepository = surveyRepository;
        this.vpiPersistenceService = vpiPersistenceService;
        this.dataPersistenceService = dataPersistenceService;
    }

    public Long save(SrvSurvey source) {
        SrvHeader header = source.header();
        SrvMachine sourceMachine = header.machine();

        Machine machine = getOrCreateMachine(sourceMachine);
        MachineParams params = getOrCreateMachineParams(machine, sourceMachine);

        Work work = saveWork(header, machine);
        saveSurvey(work, params, header);

        vpiPersistenceService.save(work.getId(), source.vpiData());
        dataPersistenceService.save(work.getId(), source);

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

    private void saveSurvey(Work work, MachineParams params, SrvHeader header) {
        Survey survey = new Survey(
                work.getId(), params, (short) header.version(),
                header.surveyTime(), header.channelMask()
        );

        setSurveyFlags(survey, header.flags());
        setSurveyTrip(survey, header.vpi());

        surveyRepository.saveAndFlush(survey);
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
}
