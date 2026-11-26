package ru.ccfit.nsu.navigator.dataservice.parser.crm.model;

// Объединённая модель заголовка и данных фрагмента выправки
public record CrmFragment(
		CrmFragmentHeader header,
		CrmFragmentData data
) {}