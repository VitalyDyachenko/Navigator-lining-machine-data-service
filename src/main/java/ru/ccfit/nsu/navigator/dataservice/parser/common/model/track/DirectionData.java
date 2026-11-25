package ru.ccfit.nsu.navigator.dataservice.parser.common.model.track;

public record DirectionData(
        int sourceId,
        short code,
        short magistralCode,
        String name,
        int startStationSourceId,
        int endStationSourceId
) {}