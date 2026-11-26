package ru.ccfit.nsu.navigator.dataservice.parser.prm.model;

// Модель флагов заголовка проекта
public record PrmFlags(
		int rawValue,
		boolean againstPicketage,
		boolean movingBackward,
		int press,
		boolean shortChord,
		boolean reservedBit5,
		boolean dlsEnabled,
		boolean warning
) {}