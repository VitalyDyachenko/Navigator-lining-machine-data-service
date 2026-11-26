package ru.ccfit.nsu.navigator.dataservice.parser.prm.model;

import ru.ccfit.nsu.navigator.dataservice.parser.common.model.GeoCoordinate;

import java.util.List;
import java.util.Map;

// Полная разобранная модель файла проекта
public record PrmCalculation(
		PrmHeader header,
		Map<Integer, byte[]> channelData,
		PrmCdtData cdtData,
		List<PrmOtvod> otvods,
		List<GeoCoordinate> geoCoordinates
) {}