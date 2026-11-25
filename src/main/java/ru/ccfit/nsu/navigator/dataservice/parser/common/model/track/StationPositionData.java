package ru.ccfit.nsu.navigator.dataservice.parser.common.model.track;

public record StationPositionData(
        int mainTrackSourceId,
        int stationSourceId,
        short axisAttributeId,
        int axisKm,
        short axisM,
        int startKm,
        short startM,
        int endKm,
        short endM
) {}