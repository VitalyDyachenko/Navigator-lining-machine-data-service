package ru.ccfit.nsu.navigator.dataservice.parser.prm.model;

import java.time.LocalDateTime;

// Модель отвода tsOTVOD
public record PrmOtvod(
		int rawFlags,
		int typeMask,
		boolean left,
		boolean right,
		boolean nonLinear,
		boolean proportional,
		boolean corrected,
		int geometryMask,
		int mode,
		int source,
		boolean autoByClearance,
		double[] startPointsM,
		double[] endPointsM,
		double[] startCoefficients,
		double[] endCoefficients,
		short[] startInitialYTenthMm,
		short[] endInitialYTenthMm,
		short deltaPlaneLeftTenthMm,
		short deltaPlaneRightTenthMm,
		short deltaProfileLeftTenthMm,
		short deltaProfileRightTenthMm,
		LocalDateTime creationTime
) {}