package ru.ccfit.nsu.navigator.dataservice.parser.crm;

import ru.ccfit.nsu.navigator.dataservice.exception.parsing.CrmParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.common.BinaryReader;
import ru.ccfit.nsu.navigator.dataservice.parser.common.MachineParser;
import ru.ccfit.nsu.navigator.dataservice.parser.common.PicketPositionParser;
import ru.ccfit.nsu.navigator.dataservice.parser.common.SurveyFlagsParser;
import ru.ccfit.nsu.navigator.dataservice.parser.common.TimeParser;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.MachineData;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.PicketPosition;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.SurveyFlags;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmChannelCalibration;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmFragmentFlags;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmFragmentHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmHardwareInfo;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmSoftwareVersion;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CrmFragmentHeaderParser {

	private static final int FRAGMENT_SIZE_V6 = 512;
	private static final int FRAGMENT_SIZE_V5 = 256;
	private static final int RUNTIME_POINTERS_SIZE = 96;
	private static final int CALIBRATION_CHANNEL_COUNT = 8;
	private static final int TAIL_RESERVED_SIZE = 64;
	private static final int BASE_CALIBRATION_YEAR = 2000;

	// Разбор таблицы описателей фрагментов выправки
	public List<CrmFragmentHeader> parseAll(BinaryReader reader, CrmHeader header) {
		int fragmentCount = header.fragmentCount();
		boolean layoutV6 = header.version() >= CrmHeaderParser.VERSION_LAYOUT_V6;
		int descriptorSize = layoutV6 ? FRAGMENT_SIZE_V6 : FRAGMENT_SIZE_V5;

		reader.requireRecordCount(fragmentCount, descriptorSize, "CRM fragment descriptor");

		MachineParser machineParser = new MachineParser();
		TimeParser timeParser = new TimeParser();
		PicketPositionParser picketParser = new PicketPositionParser();
		SurveyFlagsParser flagsParser = new SurveyFlagsParser();
		boolean forceNavigatorVpi = header.machine().rawType() == 1;

		List<CrmFragmentHeader> fragments = new ArrayList<>(fragmentCount);

		for (int i = 0; i < fragmentCount; i++) {
			fragments.add(parseSingle(
					reader,
					layoutV6,
					descriptorSize,
					forceNavigatorVpi,
					machineParser,
					timeParser,
					picketParser,
					flagsParser
			));
		}

		return fragments;
	}

	private CrmFragmentHeader parseSingle(
			BinaryReader reader,
			boolean layoutV6,
			int expectedSize,
			boolean forceNavigatorVpi,
			MachineParser machineParser,
			TimeParser timeParser,
			PicketPositionParser picketParser,
			SurveyFlagsParser flagsParser
	) {
		int startOffset = reader.position();

		MachineData machine = machineParser.parse(reader);
		LocalDateTime startTime = timeParser.parseOptional(reader);

		// Чтение сырого значения времени финиша без немедленного отказа при ошибке календаря
		long rawEndTime = reader.readUnsignedInt();
		LocalDateTime endTime = (rawEndTime == 0L) ? null : timeParser.tryDecode(rawEndTime);
		boolean brokenEndTime = rawEndTime != 0L && endTime == null;

		PicketPosition startPicket = layoutV6 ? picketParser.parse(reader) : parseLegacyPicket(reader);
		PicketPosition endPicket = layoutV6 ? picketParser.parse(reader) : parseLegacyPicket(reader);

		int startPosStepTenths = reader.readInt();
		int endPosStepTenths = reader.readInt();

		// Нормализация порядка границ IdPOS согласно спецификации DBN
		if (startPosStepTenths > endPosStepTenths) {
			int tmp = startPosStepTenths;
			startPosStepTenths = endPosStepTenths;
			endPosStepTenths = tmp;
		}

		// Пересчёт повреждённого времени финиша по длине фрагмента
		if (startTime != null && shouldRecalculateEndTime(startTime, endTime, brokenEndTime)) {
			endTime = startTime.plusSeconds((long) (endPosStepTenths - startPosStepTenths) / 3L);
		}

		CrmFragmentFlags flags = parseFlags(
				reader.readUnsignedShort(),
				layoutV6,
				forceNavigatorVpi,
				flagsParser
		);

		int channelMask = reader.readUnsignedShort();
		int stepSizeMm = reader.readUnsignedShort();
		long stepCount = reader.readUnsignedInt();
		int cycleCount = reader.readUnsignedShort();
		int markerCount = reader.readUnsignedShort();
		int rfidCount = reader.readUnsignedShort();
		int controlPointCount = reader.readUnsignedShort();
		int reservedXxxxCount = reader.readUnsignedShort();
		long reserved0 = reader.readUnsignedInt();
		long blockSize = reader.readUnsignedInt();

		// Пропуск runtime-указателей x86
		reader.skip(RUNTIME_POINTERS_SIZE);

		CrmSoftwareVersion software = parseSoftware(reader);

		if (!layoutV6) {
			String operatorText = reader.readFixedString(33);
			verifyDescriptorSize(reader, startOffset, expectedSize);
			return new CrmFragmentHeader(
					machine,
					startTime,
					endTime,
					startPicket,
					endPicket,
					startPosStepTenths,
					endPosStepTenths,
					flags,
					channelMask,
					stepSizeMm,
					stepCount,
					cycleCount,
					markerCount,
					rfidCount,
					controlPointCount,
					reservedXxxxCount,
					reserved0,
					blockSize,
					software,
					operatorText,
					false,
					0,
					0,
					0,
					new CrmHardwareInfo(0, 0, ""),
					new CrmHardwareInfo(0, 0, ""),
					new CrmHardwareInfo(0, 0, ""),
					new CrmHardwareInfo(0, 0, ""),
					new CrmChannelCalibration((short) 0, null),
					zeroCalibrations(),
					zeroCalibrations(),
					new byte[0]
			);
		}

		String operatorText = reader.readFixedString(97);
		int remarkMask = reader.readUnsignedShort();
		int startElementType = reader.readUnsignedByte();
		int endElementType = reader.readUnsignedByte();

		CrmHardwareInfo hardwareMnk = parseHardwareInfo(reader);
		CrmHardwareInfo hardwareSystem = parseHardwareInfo(reader);
		CrmHardwareInfo hardwareRfid = parseHardwareInfo(reader);
		CrmHardwareInfo hardwareRfb = parseHardwareInfo(reader);

		CrmChannelCalibration stepCalibration = parseCalibration(reader);
		List<CrmChannelCalibration> aoutCalibrations = parseCalibrations(reader);
		List<CrmChannelCalibration> ainCalibrations = parseCalibrations(reader);
		byte[] reservedTail = reader.readBytes(TAIL_RESERVED_SIZE);

		verifyDescriptorSize(reader, startOffset, expectedSize);

		return new CrmFragmentHeader(
				machine,
				startTime,
				endTime,
				startPicket,
				endPicket,
				startPosStepTenths,
				endPosStepTenths,
				flags,
				channelMask,
				stepSizeMm,
				stepCount,
				cycleCount,
				markerCount,
				rfidCount,
				controlPointCount,
				reservedXxxxCount,
				reserved0,
				blockSize,
				software,
				operatorText,
				true,
				remarkMask,
				startElementType,
				endElementType,
				hardwareMnk,
				hardwareSystem,
				hardwareRfid,
				hardwareRfb,
				stepCalibration,
				aoutCalibrations,
				ainCalibrations,
				reservedTail
		);
	}

	private boolean shouldRecalculateEndTime(
			LocalDateTime startTime,
			LocalDateTime endTime,
			boolean brokenEndTime
	) {
		if (brokenEndTime) {
			return true;
		}
		return endTime != null
				&& endTime.getYear() != startTime.getYear()
				&& startTime.getMonthValue() != 12;
	}

	// Преобразование устаревшего пикетажа tsPKT в формат tsPKT_H
	private PicketPosition parseLegacyPicket(BinaryReader reader) {
		int km = Math.max(0, reader.readInt());
		int meters = Math.max(0, reader.readShort());
		int millimeters = Math.max(0, reader.readShort());

		int picket = meters / 100;
		float meterInPicket = (meters % 100) + (millimeters / 1000.0f);
		return new PicketPosition(km, picket, meterInPicket);
	}

	private CrmFragmentFlags parseFlags(
			int rawFlags,
			boolean layoutV6,
			boolean forceNavigatorVpi,
			SurveyFlagsParser flagsParser
	) {
		SurveyFlags baseFlags = flagsParser.parse(rawFlags);
		boolean setupMark = layoutV6 && ((rawFlags >>> 14) & 1) != 0;
		boolean navigatorVpi = ((rawFlags >>> 15) & 1) != 0 || forceNavigatorVpi;

		return new CrmFragmentFlags(
				rawFlags,
				baseFlags.againstPicketage(),
				baseFlags.movingBackward(),
				baseFlags.press(),
				baseFlags.shortChord(),
				baseFlags.intertrackSpace(),
				baseFlags.markerAtFront(),
				baseFlags.dlsEnabled(),
				setupMark,
				navigatorVpi
		);
	}

	private CrmSoftwareVersion parseSoftware(BinaryReader reader) {
		int revision = reader.readUnsignedByte();
		int build = reader.readUnsignedByte();
		int minor = reader.readUnsignedByte();
		int major = reader.readUnsignedByte();
		String type = reader.readFixedString(4);
		return new CrmSoftwareVersion(type, major, minor, build, revision);
	}

	private CrmHardwareInfo parseHardwareInfo(BinaryReader reader) {
		int minor = reader.readUnsignedByte();
		int major = reader.readUnsignedByte();
		String description = reader.readFixedString(12);
		return new CrmHardwareInfo(major, minor, description);
	}

	private List<CrmChannelCalibration> parseCalibrations(BinaryReader reader) {
		List<CrmChannelCalibration> calibrations = new ArrayList<>(CALIBRATION_CHANNEL_COUNT);
		for (int i = 0; i < CALIBRATION_CHANNEL_COUNT; i++) {
			calibrations.add(parseCalibration(reader));
		}
		return calibrations;
	}

	private CrmChannelCalibration parseCalibration(BinaryReader reader) {
		int offset = reader.position();
		short msErrorTenth = reader.readShort();
		int rawDate = reader.readUnsignedShort();

		if (rawDate == 0) {
			return new CrmChannelCalibration(msErrorTenth, null);
		}

		int year = BASE_CALIBRATION_YEAR + (rawDate & 0x7F);
		int month = (rawDate >>> 7) & 0xF;
		int day = (rawDate >>> 11) & 0x1F;

		try {
			return new CrmChannelCalibration(msErrorTenth, LocalDate.of(year, month, day));
		} catch (DateTimeException e) {
			throw new CrmParsingException("Invalid calibration date at offset " + offset);
		}
	}

	private List<CrmChannelCalibration> zeroCalibrations() {
		List<CrmChannelCalibration> calibrations = new ArrayList<>(CALIBRATION_CHANNEL_COUNT);
		for (int i = 0; i < CALIBRATION_CHANNEL_COUNT; i++) {
			calibrations.add(new CrmChannelCalibration((short) 0, null));
		}
		return calibrations;
	}

	private void verifyDescriptorSize(BinaryReader reader, int startOffset, int expectedSize) {
		int actualSize = reader.position() - startOffset;
		if (actualSize != expectedSize) {
			throw new CrmParsingException(
					"Unexpected fragment descriptor size: " + actualSize + ", expected " + expectedSize
			);
		}
	}
}