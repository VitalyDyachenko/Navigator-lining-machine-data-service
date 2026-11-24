package ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiD;

public record SrvSpeed(
        int startStepIndex,
        int endStepIndex,
        int startKm,
        int startPicket,
        int endKm,
        int endPicket,
        int passengerSpeed,
        int freightSpeed,
        int emptyTrainSpeed
) {}
