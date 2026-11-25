package ru.ccfit.nsu.navigator.dataservice.parser.common.model.track;

public record MainTrackData(
        int sourceId,
        int directionSourceId,
        String number,
        short gaugeWidthId,
        int mainTrackSourceIdOsn
) {}