package ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiD;

public record SrvFragment(
        int startStepIndex,
        short startKm,
        short startM,
        short endKm,
        short endM,
        long directionStationCode,
        int parkCode,
        String trackNumber,
        int picketDirection,
        String name
) {}
