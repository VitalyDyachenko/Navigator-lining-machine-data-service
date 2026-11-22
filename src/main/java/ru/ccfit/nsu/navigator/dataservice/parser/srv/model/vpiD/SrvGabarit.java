package ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiD;

public record SrvGabarit(
        double controlDistanceM,
        int stepIndex,
        int kilometer,
        int picket,
        float meter,
        int objectType,
        int reservedFlags,
        int clearanceCode,
        int intertrackCode,
        int leftDistanceMm,
        int rightDistanceMm,
        short heightMm,
        short leftRangeMm,
        short rightRangeMm,
        String description
) {}
