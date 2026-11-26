package ru.ccfit.nsu.navigator.dataservice.parser.crm.model;

// Модель RFID-метки фрагмента tsRFID_F
public record CrmRfid(
		long timestampMs,
		long syncPulseCount,
		long tagIdentifier,
		long dataBlock,
		float odometerDeltaLenM,
		float odometerScaleAdj,
		int blockMask,
		int pointCount,
		boolean hasError,
		double controlDistanceM
) {}