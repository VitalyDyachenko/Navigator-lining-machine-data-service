package ru.ccfit.nsu.navigator.dataservice.parser.common.model;

public record SurveyFlags(
        boolean againstPicketage,
        boolean movingBackward,
        int press,
        boolean shortChord,
        int intertrackSpace,
        boolean markerAtFront,
        boolean dlsEnabled,
        int reservedFlags
) {}