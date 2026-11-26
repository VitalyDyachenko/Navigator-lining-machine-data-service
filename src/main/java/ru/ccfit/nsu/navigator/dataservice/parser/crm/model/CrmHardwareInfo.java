package ru.ccfit.nsu.navigator.dataservice.parser.crm.model;

// Модель версии аппаратного модуля tsHARDWARE
public record CrmHardwareInfo(
		int major,
		int minor,
		String description
) {}