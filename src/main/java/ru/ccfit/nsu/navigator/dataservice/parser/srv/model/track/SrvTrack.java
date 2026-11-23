package ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track;

public record SrvTrack(
        String trackNumber,
        SrvTrackPosition begin,
        SrvTrackPosition end,
        short administrativeCode,
        SrvRailway railway,
        SrvDirection direction,
        SrvMainTrack mainTrack,
        SrvPeregon peregon,
        SrvStation station1,
        SrvStationPosition stationPosition1,
        SrvStation station2,
        SrvStationPosition stationPosition2
) {}