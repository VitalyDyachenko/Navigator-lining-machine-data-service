package ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiD;

public record SrvDefect(
        int startStepIndex,
        int endStepIndex,
        int mask,
        float valueMm,
        int typeCode,
        int passengerSpeed,
        int freightSpeed,
        int emptyTrainSpeed,
        int degree,
        String label
) {}