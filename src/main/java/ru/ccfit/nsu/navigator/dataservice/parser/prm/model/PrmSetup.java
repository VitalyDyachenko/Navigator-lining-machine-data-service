package ru.ccfit.nsu.navigator.dataservice.parser.prm.model;

// Модель параметров расчёта tsSETUP_PROJECT
public record PrmSetup(
		int passengerSpeed,
		int freightSpeed,
		int maxShiftLeftMm,
		int maxShiftRightMm,
		int maxLiftMm,
		int minLiftMm,
		int reserved
) {}