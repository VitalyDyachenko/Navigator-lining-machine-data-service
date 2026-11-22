package ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiH;

public record SrvTripId(
        int reserved,
        int tripNumber,
        int wagonNumber,
        int checkCode,
        int month,
        int year,
        boolean checked,
        int railwayCode
) {}
