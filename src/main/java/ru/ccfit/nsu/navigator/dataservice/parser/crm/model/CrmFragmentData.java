package ru.ccfit.nsu.navigator.dataservice.parser.crm.model;

import ru.ccfit.nsu.navigator.dataservice.parser.common.model.marker.MarkerData;

import java.util.List;
import java.util.Map;

// Модель блока данных фрагмента выправки
public record CrmFragmentData(
		Map<Integer, byte[]> channelData,
		List<CrmPruCycle> cycles,
		List<MarkerData> markers,
		List<CrmRfid> rfids,
		List<CrmControlPoint> controlPoints
) {}