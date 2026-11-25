package ru.ccfit.nsu.navigator.dataservice.parser.common.model.track;

public record TrackPosition(
        int km,
        short meters,
        short millimeters
) {}