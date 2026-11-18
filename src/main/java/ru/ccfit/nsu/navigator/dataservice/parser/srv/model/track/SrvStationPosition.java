package ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track;

public record SrvStationPosition(
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