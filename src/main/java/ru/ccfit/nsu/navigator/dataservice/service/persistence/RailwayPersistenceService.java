package ru.ccfit.nsu.navigator.dataservice.service.persistence;

import org.springframework.stereotype.Service;
import ru.ccfit.nsu.navigator.dataservice.entity.railway.*;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.track.*;
import ru.ccfit.nsu.navigator.dataservice.repository.railway.*;

@Service
public class RailwayPersistenceService {

    private final RailwayRepository railwayRepository;
    private final DirectionRepository directionRepository;
    private final StationRepository stationRepository;
    private final MainTrackRepository mainTrackRepository;
    private final PeregonRepository peregonRepository;

    public RailwayPersistenceService(
            RailwayRepository railwayRepository,
            DirectionRepository directionRepository,
            StationRepository stationRepository,
            MainTrackRepository mainTrackRepository,
            PeregonRepository peregonRepository
    ) {
        this.railwayRepository = railwayRepository;
        this.directionRepository = directionRepository;
        this.stationRepository = stationRepository;
        this.mainTrackRepository = mainTrackRepository;
        this.peregonRepository = peregonRepository;
    }

    public Railway getOrCreateRailway(RailwayData source) {
        return railwayRepository.findByCode(source.code())
                .orElseGet(() -> railwayRepository.save(
                        new Railway(source.code(), source.name(), source.shortName())
                ));
    }

    public Direction getOrCreateDirection(DirectionData source, Railway railway) {
        return directionRepository.findByRailway_IdAndSourceId(
                railway.getId(), source.sourceId()
        ).orElseGet(() -> directionRepository.save(
                new Direction(
                        source.sourceId(),
                        railway,
                        source.code(),
                        source.magistralCode(),
                        source.name(),
                        source.startStationSourceId(),
                        source.endStationSourceId()
                )
        ));
    }

    public Station getOrCreateStation(StationData source) {
        return stationRepository.findBySourceId(source.sourceId())
                .orElseGet(() -> stationRepository.save(
                        new Station(
                                source.sourceId(),
                                source.code(),
                                source.departmentId(),
                                source.okatoId(),
                                source.typeId(),
                                source.name()
                        )
                ));
    }

    public MainTrack getOrCreateMainTrack(MainTrackData source, Direction direction) {
        return mainTrackRepository.findByDirection_IdAndSourceId(
                direction.getId(), source.sourceId()
        ).orElseGet(() -> mainTrackRepository.save(
                new MainTrack(
                        source.sourceId(),
                        direction,
                        source.number(),
                        source.gaugeWidthId(),
                        source.mainTrackSourceIdOsn()
                )
        ));
    }

    public Peregon getOrCreatePeregon(
            PeregonData source,
            Direction direction,
            Station startStation,
            Station endStation
    ) {
        return peregonRepository.findByDirection_IdAndSourceId(
                direction.getId(), source.sourceId()
        ).orElseGet(() -> peregonRepository.save(
                new Peregon(
                        source.sourceId(),
                        direction,
                        startStation,
                        endStation,
                        source.exploitationLength(),
                        source.name()
                )
        ));
    }
}