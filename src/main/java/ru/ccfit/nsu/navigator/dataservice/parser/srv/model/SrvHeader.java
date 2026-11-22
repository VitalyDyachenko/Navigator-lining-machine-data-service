package ru.ccfit.nsu.navigator.dataservice.parser.srv.model;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.channelInfo.SrvChannelInfo;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track.SrvTrack;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiH.SrvVpiHeader;

import java.time.LocalDateTime;

public record SrvHeader(
        String signature,
        int version,
        LocalDateTime surveyTime,
        SrvMachine machine,
        SrvSurveyFlags flags,
        int channelMask,
        int stepSizeMm,
        long stepCount,
        int markerCount,
        int rfidCount,

        SrvTrack track,                 // Track — описание участка пути
        SrvVpiHeader vpi,               // VPI — информация о проезде
        SrvChannelInfo[] channels,      // InfoCH[16] — сведения о каналах
        byte[] undocumentedHeaderBytes // Неописанная область после InfoCH
) {}