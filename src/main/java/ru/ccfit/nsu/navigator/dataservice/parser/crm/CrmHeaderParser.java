package ru.ccfit.nsu.navigator.dataservice.parser.crm;

import ru.ccfit.nsu.navigator.dataservice.exception.parsing.CrmParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.common.BinaryReader;
import ru.ccfit.nsu.navigator.dataservice.parser.common.MachineParser;
import ru.ccfit.nsu.navigator.dataservice.parser.common.SurveyFlagsParser;
import ru.ccfit.nsu.navigator.dataservice.parser.common.TimeParser;
import ru.ccfit.nsu.navigator.dataservice.parser.common.TrackParser;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.MachineData;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.SurveyFlags;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.track.TrackData;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmHeader;

import java.time.LocalDateTime;

public class CrmHeaderParser {

	public static final int VERSION_CORRECT_5 = 0x0501;
	public static final int VERSION_CORRECT = 0x0606;
	public static final int VERSION_LAYOUT_V6 = 0x0600;

	private static final int HEADER_SIZE = 0x300;
	private static final int USED_HEADER_SIZE = 491;

	// Разбор фиксированного заголовка истории выправки длиной 768 байт
	public CrmHeader parse(BinaryReader reader) {
		String signature = reader.readAsciiString(6);
		if (!signature.equals("NavCRM")) {
			throw new CrmParsingException("Invalid CRM file signature: " + signature);
		}

		int version = reader.readUnsignedShort();
		if (!isSupportedVersion(version)) {
			throw new CrmParsingException(
					String.format("Unsupported CRM version: 0x%04X", version)
			);
		}

		LocalDateTime correctionTime = new TimeParser().parseOptional(reader);
		MachineData machine = new MachineParser().parse(reader);
		SurveyFlags flags = new SurveyFlagsParser().parse(reader);

		int channelMask = reader.readUnsignedShort();
		int stepSizeMm = reader.readUnsignedShort();
		long stepCount = reader.readUnsignedInt();
		int fragmentCount = reader.readUnsignedShort();
		int rfidCount = reader.readUnsignedShort();
		TrackData track = new TrackParser().parse(reader);
		byte[] paddingBytes = reader.readBytes(HEADER_SIZE - USED_HEADER_SIZE);

		return new CrmHeader(
				signature,
				version,
				correctionTime,
				machine,
				flags,
				channelMask,
				stepSizeMm,
				stepCount,
				fragmentCount,
				rfidCount,
				track,
				paddingBytes
		);
	}

	private boolean isSupportedVersion(int version) {
		return version == VERSION_CORRECT_5 || version == VERSION_CORRECT;
	}
}