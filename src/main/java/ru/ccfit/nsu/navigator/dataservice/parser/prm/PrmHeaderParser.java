package ru.ccfit.nsu.navigator.dataservice.parser.prm;

import ru.ccfit.nsu.navigator.dataservice.exception.parsing.PrmParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.common.BinaryReader;
import ru.ccfit.nsu.navigator.dataservice.parser.common.ChannelInfoParser;
import ru.ccfit.nsu.navigator.dataservice.parser.common.MachineParser;
import ru.ccfit.nsu.navigator.dataservice.parser.common.TimeParser;
import ru.ccfit.nsu.navigator.dataservice.parser.common.TrackParser;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.ChannelInfo;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.MachineData;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.track.TrackData;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmCdtHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmExtraExit;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmFlags;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmSetup;

import java.time.LocalDateTime;

public class PrmHeaderParser {

	public static final int VERSION_PRJ_5 = 0x0501;
	public static final int VERSION_PRJ_M = 0x0605;
	public static final int VERSION_PRJ_7 = 0x0701;

	private static final int HEADER_SIZE = 0x300;
	private static final int USED_SIZE_06XX = 659;
	private static final int USED_SIZE_0701 = 661;

	// Разбор фиксированного заголовка проекта длиной 768 байт
	public PrmHeader parse(BinaryReader reader) {
		String signature = reader.readAsciiString(6);
		if (!signature.equals("NavPRM")) {
			throw new PrmParsingException("Invalid PRM file signature: " + signature);
		}

		int version = reader.readUnsignedShort();
		if (!isSupportedVersion(version)) {
			throw new PrmParsingException(
					String.format("Unsupported PRM version: 0x%04X", version)
			);
		}

		TimeParser timeParser = new TimeParser();
		LocalDateTime calculationTime = timeParser.parseOptional(reader);
		MachineData machine = new MachineParser().parse(reader);
		PrmExtraExit extraExit = parseExtraExit(reader);

		int rawFlags = reader.readUnsignedShort();
		PrmFlags flags = parseFlags(rawFlags);

		// В версии 0x0701 поле MaskCH вынесено в отдельное 16-битное слово
		int channelMask = (version >= VERSION_PRJ_7)
				? reader.readUnsignedShort()
				: ((rawFlags >>> 8) & 0xFF);

		int stepSizeMm = reader.readUnsignedShort();
		long stepCount = reader.readUnsignedInt();
		int otvodCount = reader.readUnsignedShort();

		PrmSetup setup = parseSetup(reader);
		LocalDateTime surveyTime = timeParser.parseOptional(reader);
		TrackData track = normalizeTrackBounds(new TrackParser().parse(reader));
		String computerName = reader.readFixedString(24);
		ChannelInfo[] channels = new ChannelInfoParser().parse(reader);
		PrmCdtHeader cdtHeader = parseCdtHeader(reader);

		int paddingSize = HEADER_SIZE - (version >= VERSION_PRJ_7 ? USED_SIZE_0701 : USED_SIZE_06XX);
		byte[] paddingBytes = reader.readBytes(paddingSize);

		return new PrmHeader(
				signature,
				version,
				calculationTime,
				machine,
				extraExit,
				flags,
				channelMask,
				stepSizeMm,
				stepCount,
				otvodCount,
				setup,
				surveyTime,
				track,
				computerName,
				channels,
				cdtHeader,
				paddingBytes
		);
	}

	private boolean isSupportedVersion(int version) {
		return version == VERSION_PRJ_5
				|| version == VERSION_PRJ_M
				|| version == VERSION_PRJ_7;
	}

	private PrmExtraExit parseExtraExit(BinaryReader reader) {
		int machinePos = reader.readInt();
		int planeFrontPos = reader.readInt();
		long timeSeconds = reader.readUnsignedInt();
		int rawFlags = reader.readUnsignedShort();
		int reserved = reader.readUnsignedShort();

		return new PrmExtraExit(
				machinePos,
				planeFrontPos,
				timeSeconds,
				rawFlags,
				(rawFlags & 1) != 0,
				((rawFlags >>> 1) & 1) != 0,
				(rawFlags >>> 2) & 0b11,
				((rawFlags >>> 4) & 1) != 0,
				(rawFlags >>> 5) & 0b11,
				((rawFlags >>> 7) & 1) != 0,
				((rawFlags >>> 8) & 1) != 0,
				(rawFlags >>> 9) & 0xF,
				(rawFlags >>> 13) & 0b111,
				reserved
		);
	}

	private PrmFlags parseFlags(int rawFlags) {
		return new PrmFlags(
				rawFlags,
				(rawFlags & 1) != 0,
				((rawFlags >>> 1) & 1) != 0,
				(rawFlags >>> 2) & 0b11,
				((rawFlags >>> 4) & 1) != 0,
				((rawFlags >>> 5) & 1) != 0,
				((rawFlags >>> 6) & 1) != 0,
				((rawFlags >>> 7) & 1) != 0
		);
	}

	private PrmSetup parseSetup(BinaryReader reader) {
		return new PrmSetup(
				reader.readUnsignedShort(),
				reader.readUnsignedShort(),
				reader.readInt(),
				reader.readInt(),
				reader.readInt(),
				reader.readInt(),
				reader.readInt()
		);
	}

	private PrmCdtHeader parseCdtHeader(BinaryReader reader) {
		int rawFlags = reader.readUnsignedShort();
		short baseOffsetMm = reader.readShort();
		int objectsCount = reader.readInt();

		return new PrmCdtHeader(
				rawFlags,
				rawFlags & 0b11,
				(rawFlags >>> 2) & 0xF,
				(rawFlags >>> 6) & 0x3FF,
				baseOffsetMm,
				objectsCount
		);
	}

	// Перестановка границ участка при инвертированном километраже по спецификации DBN
	private TrackData normalizeTrackBounds(TrackData track) {
		if (track.begin().km() <= track.end().km()) {
			return track;
		}

		return new TrackData(
				track.trackNumber(),
				track.end(),
				track.begin(),
				track.administrativeCode(),
				track.railway(),
				track.direction(),
				track.mainTrack(),
				track.peregon(),
				track.station1(),
				track.stationPosition1(),
				track.station2(),
				track.stationPosition2()
		);
	}
}