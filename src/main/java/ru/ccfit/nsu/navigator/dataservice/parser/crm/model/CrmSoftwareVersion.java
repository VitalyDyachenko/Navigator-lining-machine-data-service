package ru.ccfit.nsu.navigator.dataservice.parser.crm.model;

// Модель версии ПО tsSOFTWARE
public record CrmSoftwareVersion(
		String type,
		int major,
		int minor,
		int build,
		int revision
) {}