package ru.ccfit.nsu.navigator.dataservice.parser.prm.model;

import ru.ccfit.nsu.navigator.dataservice.parser.common.model.ChannelInfo;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.MachineData;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.track.TrackData;

import java.time.LocalDateTime;

// Модель заголовка файла проекта
public record PrmHeader(
		String signature,
		int version,
		LocalDateTime calculationTime,
		MachineData machine,
		PrmExtraExit extraExit,
		PrmFlags flags,
		int channelMask,
		int stepSizeMm,
		long stepCount,
		int otvodCount,
		PrmSetup setup,
		LocalDateTime surveyTime,
		TrackData track,
		String computerName,
		ChannelInfo[] channels,
		PrmCdtHeader cdtHeader,
		byte[] paddingBytes
) {}