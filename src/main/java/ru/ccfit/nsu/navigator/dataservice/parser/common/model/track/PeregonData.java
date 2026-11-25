package ru.ccfit.nsu.navigator.dataservice.parser.common.model.track;

public record PeregonData(
        int sourceId,
        int directionSourceId,
        int startStationSourceId,
        int endStationSourceId,
        double exploitationLength,
        String name
) {}
