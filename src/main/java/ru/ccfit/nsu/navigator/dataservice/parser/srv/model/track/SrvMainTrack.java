package ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track;

public record SrvMainTrack(
        int sourceId,
        int directionSourceId,
        String number,
        short gaugeWidthId,
        int mainTrackSourceIdOsn
) {}