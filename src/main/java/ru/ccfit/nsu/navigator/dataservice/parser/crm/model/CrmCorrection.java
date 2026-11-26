package ru.ccfit.nsu.navigator.dataservice.parser.crm.model;

import java.util.List;

// Полная разобранная модель файла истории выправки
public record CrmCorrection(
		CrmHeader header,
		List<CrmFragment> fragments
) {}