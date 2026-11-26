package ru.ccfit.nsu.navigator.dataservice.parser.prm.model;

import java.util.List;

// Траектории измеренной и проектной осей CDT
public record PrmCdtData(
		List<PrmCdtPoint> measuredAxis,
		List<PrmCdtPoint> projectAxis
) {}