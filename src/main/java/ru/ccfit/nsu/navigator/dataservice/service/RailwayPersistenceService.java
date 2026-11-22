package ru.ccfit.nsu.navigator.dataservice.service;

import org.springframework.stereotype.Service;
import ru.ccfit.nsu.navigator.dataservice.entity.railway.Direction;
import ru.ccfit.nsu.navigator.dataservice.entity.railway.Railway;
import ru.ccfit.nsu.navigator.dataservice.entity.railway.Station;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track.*;
import ru.ccfit.nsu.navigator.dataservice.repository.railway.DirectionRepository;
import ru.ccfit.nsu.navigator.dataservice.repository.railway.RailwayRepository;
import ru.ccfit.nsu.navigator.dataservice.repository.railway.StationRepository;

@Service
public class RailwayPersistenceService {

    private final RailwayRepository railwayRepository;
    private final DirectionRepository directionRepository;
    private final StationRepository stationRepository;

    public RailwayPersistenceService(RailwayRepository railwayRepository,
                                     DirectionRepository directionRepository,
                                     StationRepository stationRepository) {
        this.railwayRepository = railwayRepository;
        this.directionRepository = directionRepository;
        this.stationRepository = stationRepository;
    }

    public Railway getOrCreateRailway(SrvRailway source) {
        return railwayRepository.findByCode(source.code())
                .orElseGet(() -> railwayRepository.save(
                        new Railway(source.code(), source.name(), source.shortName())
                ));
    }

    public Direction getOrCreateDirection(SrvDirection source, Railway railway) {
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

    public Station getOrCreateStation(SrvStation source) {
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

}
