package ru.ccfit.nsu.navigator.dataservice.parser.srv.model;

public record SrvSurveyFlags(
        boolean againstPicketage,
        boolean movingBackward,
        int press,
        boolean shortChord,
        int intertrackSpace,
        boolean markerAtFront,
        boolean dlsEnabled,
        int reservedFlags
) {}