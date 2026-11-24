package ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiD;

public record SrvSwitch(
        int stepIndex,
        int direction,
        int type,
        int lengthM,
        String number,
        int stationCode,
        boolean fromPassport
) {}