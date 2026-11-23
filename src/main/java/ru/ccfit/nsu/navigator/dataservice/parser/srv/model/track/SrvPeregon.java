package ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track;

public record SrvPeregon(
        int sourceId,
        int directionSourceId,
        int startStationSourceId,
        int endStationSourceId,
        double exploitationLength,
        String name
) {}
