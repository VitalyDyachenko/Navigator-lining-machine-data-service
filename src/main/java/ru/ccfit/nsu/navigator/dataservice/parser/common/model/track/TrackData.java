package ru.ccfit.nsu.navigator.dataservice.parser.common.model.track;

public record TrackData(
        String trackNumber,
        TrackPosition begin,
        TrackPosition end,
        short administrativeCode,
        RailwayData railway,
        DirectionData direction,
        MainTrackData mainTrack,
        PeregonData peregon,
        StationData station1,
        StationPositionData stationPosition1,
        StationData station2,
        StationPositionData stationPosition2
) {}