package ru.ccfit.nsu.navigator.dataservice.parser.crm.model;

import ru.ccfit.nsu.navigator.dataservice.parser.common.model.MachineData;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.PicketPosition;

import java.time.LocalDateTime;
import java.util.List;

// Модель описателя фрагмента выправки tsFRAGMENT
public record CrmFragmentHeader(
		MachineData machine,
		LocalDateTime startTime,
		LocalDateTime endTime,
		PicketPosition startPicket,
		PicketPosition endPicket,
		int startPosStepTenths,
		int endPosStepTenths,
		CrmFragmentFlags flags,
		int channelMask,
		int stepSizeMm,
		long stepCount,
		int cycleCount,
		int markerCount,
		int rfidCount,
		int controlPointCount,
		int reservedXxxxCount,
		long reserved0,
		long blockSize,
		CrmSoftwareVersion software,
		String operatorText,
		boolean hasExtendedMetadata,
		int remarkMask,
		int startElementType,
		int endElementType,
		CrmHardwareInfo hardwareMnk,
		CrmHardwareInfo hardwareSystem,
		CrmHardwareInfo hardwareRfid,
		CrmHardwareInfo hardwareRfb,
		CrmChannelCalibration stepCalibration,
		List<CrmChannelCalibration> aoutCalibrations,
		List<CrmChannelCalibration> ainCalibrations,
		byte[] reservedTail
) {}