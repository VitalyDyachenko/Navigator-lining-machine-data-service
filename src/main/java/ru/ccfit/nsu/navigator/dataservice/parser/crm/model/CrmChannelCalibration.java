package ru.ccfit.nsu.navigator.dataservice.parser.crm.model;

import java.time.LocalDate;

// Модель калибровочных данных канала tsSYS_CH_INFO
public record CrmChannelCalibration(
		short msErrorTenth,
		LocalDate calibrationDate
) {}