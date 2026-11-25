package ru.ccfit.nsu.navigator.dataservice.parser.srv.model;

import ru.ccfit.nsu.navigator.dataservice.parser.common.model.ChannelInfo;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.MachineData;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.SurveyFlags;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.track.TrackData;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiH.SrvVpiHeader;

import java.time.LocalDateTime;

public record SrvHeader(
		String signature,
		int version,
		LocalDateTime surveyTime,
		MachineData machine,
		SurveyFlags flags,
		int channelMask,
		int stepSizeMm,
		long stepCount,
		int markerCount,
		int rfidCount,

		TrackData track,                 // Track — описание участка пути
		SrvVpiHeader vpi,               // VPI — информация о проезде
		ChannelInfo[] channels,      // InfoCH[16] — сведения о каналах
		byte[] undocumentedHeaderBytes // Неописанная область после InfoCH
) {}