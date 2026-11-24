package ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiD;

public record SrvBuild(
        int startStepIndex,
        int endStepIndex,
        short type,
        boolean fromPassport
) {}