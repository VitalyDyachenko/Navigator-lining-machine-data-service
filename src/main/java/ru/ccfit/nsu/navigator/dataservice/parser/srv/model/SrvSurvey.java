package ru.ccfit.nsu.navigator.dataservice.parser.srv.model;

import java.util.List;
import java.util.Map;

import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.marker.SrvMarker;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.rfid.SrvRfid;

public record SrvSurvey(
        SrvHeader header,
        Map<Integer, byte[]> channelData,
        List<SrvMarker> markers,
        List<SrvRfid> rfids,
        SrvVpiData vpiData
) {}
