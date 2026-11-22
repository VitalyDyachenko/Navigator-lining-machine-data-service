package ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiD;

public record SrvCurve(
        int stepIndex,
        float radiusM,
        float gaugeNormMm,
        float wearMm,
        float toleranceMm
) {}