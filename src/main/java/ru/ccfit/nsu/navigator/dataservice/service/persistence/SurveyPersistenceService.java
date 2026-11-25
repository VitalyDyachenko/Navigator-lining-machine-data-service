
package ru.ccfit.nsu.navigator.dataservice.service.persistence;

import org.springframework.stereotype.Service;
import ru.ccfit.nsu.navigator.dataservice.entity.machine.Machine;
import ru.ccfit.nsu.navigator.dataservice.entity.machine.MachineParams;
import ru.ccfit.nsu.navigator.dataservice.entity.railway.*;
import ru.ccfit.nsu.navigator.dataservice.entity.survey.Survey;
import ru.ccfit.nsu.navigator.dataservice.entity.work.Work;
import ru.ccfit.nsu.navigator.dataservice.exception.parsing.SrvParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.*;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track.SrvTrack;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiH.*;
import ru.ccfit.nsu.navigator.dataservice.repository.machine.MachineParamsRepository;
import ru.ccfit.nsu.navigator.dataservice.repository.machine.MachineRepository;
import ru.ccfit.nsu.navigator.dataservice.repository.survey.SurveyRepository;
import ru.ccfit.nsu.navigator.dataservice.repository.work.WorkRepository;
import ru.ccfit.nsu.navigator.dataservice.repository.railway.StationPositionRepository;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track.SrvStationPosition;

import java.util.List;

@Service
public class SurveyPersistenceService {

    private final MachineRepository machineRepository;
    private final MachineParamsRepository machineParamsRepository;
    private final WorkRepository workRepository;
    private final SurveyRepository surveyRepository;
    private final SurveyVpiPersistenceService vpiPersistenceService;
    private final SurveyDataPersistenceService dataPersistenceService;
    private final RailwayPersistenceService railwayPersistenceService;
    private final StationPositionRepository stationPositionRepository;


    public SurveyPersistenceService(
            MachineRepository machineRepository,
            MachineParamsRepository machineParamsRepository,
            WorkRepository workRepository,
            SurveyRepository surveyRepository,
            SurveyVpiPersistenceService vpiPersistenceService,
            SurveyDataPersistenceService dataPersistenceService,
            RailwayPersistenceService railwayPersistenceService,
            StationPositionRepository stationPositionRepository
    ) {
        this.machineRepository = machineRepository;
        this.machineParamsRepository = machineParamsRepository;
        this.workRepository = workRepository;
        this.surveyRepository = surveyRepository;
        this.vpiPersistenceService = vpiPersistenceService;
        this.dataPersistenceService = dataPersistenceService;
        this.railwayPersistenceService = railwayPersistenceService;
        this.stationPositionRepository = stationPositionRepository;
    }


    public Long save(SrvSurvey source) {
        SrvHeader header = source.header();
        SrvMachine sourceMachine = header.machine();

        Machine machine = getOrCreateMachine(sourceMachine);
        MachineParams params = getOrCreateMachineParams(machine, sourceMachine);


        SrvTrack track = header.track();
        validateTrackReferences(track);

        Railway railway = railwayPersistenceService.getOrCreateRailway(track.railway());
        Direction direction = railwayPersistenceService.getOrCreateDirection(
                track.direction(), railway
        );

        Station firstStation = railwayPersistenceService.getOrCreateStation(track.station1());
        Station secondStation = railwayPersistenceService.getOrCreateStation(track.station2());

        MainTrack mainTrack = railwayPersistenceService.getOrCreateMainTrack(
                track.mainTrack(), direction
        );

        Peregon peregon = railwayPersistenceService.getOrCreatePeregon(
                track.peregon(), direction, firstStation, secondStation
        );

        Work work = saveWork(header, machine, mainTrack, peregon);

        saveStationPositions(
                work.getId(),
                track,
                firstStation,
                secondStation
        );

        saveSurvey(work, params, header);

        vpiPersistenceService.save(work.getId(), source.vpiData());
        dataPersistenceService.save(work.getId(), source);


        return work.getId();
    }


    private void validateTrackReferences(SrvTrack track) {
        int directionId = track.direction().sourceId();

        if ((track.mainTrack().directionSourceId() != 0
                && track.mainTrack().directionSourceId() != directionId)
                || track.peregon().directionSourceId() != directionId
                || track.peregon().startStationSourceId() != track.station1().sourceId()
                || track.peregon().endStationSourceId() != track.station2().sourceId()) {

            throw new SrvParsingException("Inconsistent railway references in SRV");
        }

        validateStationPositionReferences(
                track.stationPosition1(),
                track.station1().sourceId(),
                track.mainTrack().sourceId(),
                "STANKM1"
        );

        validateStationPositionReferences(
                track.stationPosition2(),
                track.station2().sourceId(),
                track.mainTrack().sourceId(),
                "STANKM2"
        );
    }

    private void validateStationPositionReferences(
            SrvStationPosition position,
            int expectedStationSourceId,
            int expectedMainTrackSourceId,
            String positionName
    ) {
        if (position.stationSourceId() != expectedStationSourceId) {
            throw new SrvParsingException(
                    positionName + ": station source ID mismatch"
            );
        }

        if (position.mainTrackSourceId() != expectedMainTrackSourceId) {
            throw new SrvParsingException(
                    positionName + ": main track source ID mismatch"
            );
        }
    }



    private Machine getOrCreateMachine(SrvMachine source) {
        if (source.machineType() < 0 || source.machineType() > 4) {
            throw new SrvParsingException("Unsupported machine type: " + source.machineType());
        }

        short typeId = (short) source.machineType();

        machineRepository.insertIfMissing(
                source.number(),
                source.name(),
                typeId
        );

        return machineRepository.findByNumberAndNameAndTypeId(
                source.number(),
                source.name(),
                typeId
        ).orElseThrow(() -> new IllegalStateException(
                "Machine not found after insertIfMissing"
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


    private Work saveWork(SrvHeader header, Machine machine,
                          MainTrack mainTrack, Peregon peregon) {
        SrvTrack track = header.track();
        Work work = Work.createPending();

        work.setMachine(machine);
        work.setMainTrack(mainTrack);
        work.setPeregon(peregon);

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

        // Если данные проезда ВПИ отсутствуют,
        // не заполняем поля survey фиктивными нулями.
        if (vpi.fragmentCount() == 0
                && vpi.startStep() == 0
                && trip.tripNumber() == 0
                && trip.wagonNumber() == 0
                && trip.year() == 0
                && trip.month() == 0
                && trip.railwayCode() == 0
                && trip.checkCode() == 0
                && !trip.checked()) {
            return;
        }

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

    private void saveStationPositions(
            Long workId,
            SrvTrack track,
            Station firstStation,
            Station secondStation
    ) {
        StationPosition firstPosition = createStationPosition(
                workId,
                (short) 1,
                firstStation,
                track.stationPosition1()
        );

        StationPosition secondPosition = createStationPosition(
                workId,
                (short) 2,
                secondStation,
                track.stationPosition2()
        );

        stationPositionRepository.saveAll(
                List.of(firstPosition, secondPosition)
        );
    }

    private StationPosition createStationPosition(
            Long workId,
            short order,
            Station station,
            SrvStationPosition source
    ) {
        return new StationPosition(
                workId,
                order,
                station,
                source.axisAttributeId(),
                source.axisKm(),
                source.axisM(),
                source.startKm(),
                source.startM(),
                source.endKm(),
                source.endM()
        );
    }

}
