package ru.ccfit.nsu.navigator.dataservice.parser.crm.model;

import java.time.LocalTime;

// Модель контрольной точки при выправке tsCTRL_POINT
public record CrmControlPoint(
		int regimCode,
		int tempCtrl,
		LocalTime recordedTime,
		short[] interfaceRegulators,
		short[] externalRegulators,
		short leftRailTempC,
		short rightRailTempC,
		short airTempC
) {}