package ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track;

public record SrvTrackPosition(
        int km,
        short meters,
        short millimeters
) {}