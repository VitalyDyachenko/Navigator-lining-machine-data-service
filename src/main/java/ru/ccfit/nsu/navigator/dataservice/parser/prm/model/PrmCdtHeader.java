package ru.ccfit.nsu.navigator.dataservice.parser.prm.model;

// Модель заголовка координатного описания пути tsCDT_H
public record PrmCdtHeader(
		int rawFlags,
		int baseRail,
		int pointsMask,
		int reservedFlags,
		short baseOffsetMm,
		int objectsCount
) {}