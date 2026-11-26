package ru.ccfit.nsu.navigator.dataservice.parser.prm.model;

// Модель структуры tsEXTRA_EXIT
public record PrmExtraExit(
		int machinePos,
		int planeFrontPos,
		long timeSeconds,
		int rawFlags,
		boolean againstPicketage,
		boolean movingBackward,
		int press,
		boolean shortChord,
		int intertrackSpace,
		boolean markerAtFront,
		boolean dlsEnabled,
		int reservedFlags,
		int status,
		int reserved
) {}