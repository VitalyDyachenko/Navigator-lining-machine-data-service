package ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track;

public record SrvDirection(
        int sourceId,
        short code,
        short magistralCode,
        String name,
        int startStationSourceId,
        int endStationSourceId
) {}