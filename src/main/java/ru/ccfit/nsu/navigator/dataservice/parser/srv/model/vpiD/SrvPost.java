package ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiD;


public record SrvPost(
        int stepIndex,
        int kilometerNumber,
        boolean fromPassport
) {}
