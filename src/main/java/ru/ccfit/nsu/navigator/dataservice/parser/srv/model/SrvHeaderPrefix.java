package ru.ccfit.nsu.navigator.dataservice.parser.srv.model;

import java.time.LocalDateTime;

public record SrvHeaderPrefix(
        String signature,
        int version,
        LocalDateTime surveyTime,
        SrvMachine machine,
        SrvSurveyFlags flags,
        int channelMask,
        int stepSizeMm,
        long stepCount,
        int markerCount,
        int rfidCount
) {}