package ru.ccfit.nsu.navigator.dataservice.parser.srv.model;

import ru.ccfit.nsu.navigator.dataservice.parser.common.model.marker.MarkerData;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.rfid.SrvRfid;

import java.util.List;
import java.util.Map;

public record SrvSurvey(
		SrvHeader header,
		Map<Integer, byte[]> channelData,
		List<MarkerData> markers,
		List<SrvRfid> rfids,
		SrvVpiData vpiData
) {}