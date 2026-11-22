package ru.ccfit.nsu.navigator.dataservice.service;

import org.springframework.stereotype.Service;
import ru.ccfit.nsu.navigator.dataservice.entity.*;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track.*;
import ru.ccfit.nsu.navigator.dataservice.repository.*;

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
        Direction direction = new Direction(
                source.sourceId(),
                railway,
                source.code(),
                source.magistralCode(),
                source.name(),
                source.startStationSourceId(),
                source.endStationSourceId()
        );

        return directionRepository.findAllByRailway_IdAndSourceId(
                        railway.getId(), source.sourceId()
                )
                .stream()
                .filter(existing -> existing.hasSameData(direction))
                .findFirst()
                .orElseGet(() -> directionRepository.save(direction));
    }

    public Station getOrCreateStation(SrvStation source) {
        Station station = new Station(
                source.sourceId(),
                source.code(),
                source.departmentId(),
                source.okatoId(),
                source.typeId(),
                source.name()
        );

        return stationRepository.findAllBySourceId(source.sourceId())
                .stream()
                .filter(existing -> existing.hasSameData(station))
                .findFirst()
                .orElseGet(() -> stationRepository.save(station));
    }
}
