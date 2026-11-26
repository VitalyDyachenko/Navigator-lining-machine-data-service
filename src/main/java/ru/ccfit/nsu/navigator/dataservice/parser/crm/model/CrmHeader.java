package ru.ccfit.nsu.navigator.dataservice.parser.crm.model;

import ru.ccfit.nsu.navigator.dataservice.parser.common.model.MachineData;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.SurveyFlags;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.track.TrackData;

import java.time.LocalDateTime;

// Модель заголовка файла истории выправки tsHEADER_CORRECT
public record CrmHeader(
		String signature,
		int version,
		LocalDateTime correctionTime,
		MachineData machine,
		SurveyFlags flags,
		int channelMask,
		int stepSizeMm,
		long stepCount,
		int fragmentCount,
		int rfidCount,
		TrackData track,
		byte[] paddingBytes
) {}