package ru.ccfit.nsu.navigator.dataservice.parser.crm.model;

// Модель флагов фрагмента выправки
public record CrmFragmentFlags(
		int rawValue,
		boolean againstPicketage,
		boolean movingBackward,
		int press,
		boolean shortChord,
		int intertrackSpace,
		boolean markerAtFront,
		boolean dlsEnabled,
		boolean setupMark,
		boolean navigatorVpi
) {}