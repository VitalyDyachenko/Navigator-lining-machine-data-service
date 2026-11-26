package ru.ccfit.nsu.navigator.dataservice.parser.crm;

import ru.ccfit.nsu.navigator.dataservice.exception.parsing.CrmParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.common.BinaryReader;
import ru.ccfit.nsu.navigator.dataservice.parser.common.MarkerParser;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.marker.MarkerData;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmControlPoint;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmFragmentData;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmFragmentHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmPruCycle;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmRfid;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CrmFragmentDataParser {

	private static final int CHANNEL_COUNT = 16;
	private static final int PRU_CYCLE_SIZE = 16;
	private static final int MARKER_SIZE = 5;
	private static final int RFID_SIZE = 34;
	private static final int CTRL_POINT_SIZE = 20;
	private static final int INTERFACE_REGULATOR_COUNT = 8;
	private static final int EXTERNAL_REGULATOR_COUNT = 5;

	// Разбор блока данных фрагмента и контроль длины SizeBLOCK
	public CrmFragmentData parse(
			BinaryReader reader,
			CrmHeader fileHeader,
			CrmFragmentHeader fragmentHeader
	) {
		verifyBlockSize(reader, fragmentHeader);
		int blockStartOffset = reader.position();

		Map<Integer, byte[]> channelData = parseChannels(reader, fragmentHeader);
		List<CrmPruCycle> cycles = parseCycles(reader, fileHeader.stepCount(), fragmentHeader.cycleCount());
		List<MarkerData> markers = new MarkerParser().parse(reader, fragmentHeader.markerCount());
		List<CrmRfid> rfids = parseRfids(
				reader,
				fileHeader.version(),
				fragmentHeader.stepSizeMm(),
				fragmentHeader.rfidCount()
		);
		List<CrmControlPoint> controlPoints = parseControlPoints(reader, fragmentHeader.controlPointCount());

		long actualBlockSize = reader.position() - blockStartOffset;
		if (actualBlockSize != fragmentHeader.blockSize()) {
			throw new CrmParsingException(
					"Fragment data size mismatch: read " + actualBlockSize
							+ " bytes, expected " + fragmentHeader.blockSize()
			);
		}

		return new CrmFragmentData(channelData, cycles, markers, rfids, controlPoints);
	}

	// Декодирование 16-битных знаковых отсчётов канала фрагмента
	public short[] decodeShortValues(byte[] data) {
		if (data.length % Short.BYTES != 0) {
			throw new CrmParsingException("Invalid short channel data length: " + data.length);
		}

		ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
		short[] values = new short[data.length / Short.BYTES];

		for (int i = 0; i < values.length; i++) {
			values[i] = buffer.getShort();
		}

		return values;
	}

	private void verifyBlockSize(BinaryReader reader, CrmFragmentHeader fragmentHeader) {
		int activeChannels = Integer.bitCount(fragmentHeader.channelMask());
		long expectedSize = (long) activeChannels * fragmentHeader.stepCount() * Short.BYTES
				+ (long) fragmentHeader.cycleCount() * PRU_CYCLE_SIZE
				+ (long) fragmentHeader.markerCount() * MARKER_SIZE
				+ (long) fragmentHeader.rfidCount() * RFID_SIZE
				+ (long) fragmentHeader.controlPointCount() * CTRL_POINT_SIZE;

		if (expectedSize != fragmentHeader.blockSize()) {
			throw new CrmParsingException(
					"Fragment SizeBLOCK mismatch: declared " + fragmentHeader.blockSize()
							+ ", calculated " + expectedSize
			);
		}

		if (fragmentHeader.blockSize() > reader.remaining()) {
			throw new CrmParsingException(
					"Fragment block exceeds remaining file size: " + fragmentHeader.blockSize()
			);
		}
	}

	private Map<Integer, byte[]> parseChannels(BinaryReader reader, CrmFragmentHeader fragmentHeader) {
		int mask = fragmentHeader.channelMask();
		long bytesPerChannelLong = fragmentHeader.stepCount() * Short.BYTES;

		if (bytesPerChannelLong > Integer.MAX_VALUE) {
			throw new CrmParsingException("Fragment channel data is too large: " + bytesPerChannelLong);
		}

		int bytesPerChannel = (int) bytesPerChannelLong;
		Map<Integer, byte[]> channels = new LinkedHashMap<>();

		for (int i = 0; i < CHANNEL_COUNT; i++) {
			if ((mask & (1 << i)) == 0) {
				continue;
			}
			channels.put(i, reader.readBytes(bytesPerChannel));
		}

		return channels;
	}

	private List<CrmPruCycle> parseCycles(BinaryReader reader, long headerStepCount, int cycleCount) {
		reader.requireRecordCount(cycleCount, PRU_CYCLE_SIZE, "PRU cycle");
		List<CrmPruCycle> cycles = new ArrayList<>();

		for (int i = 0; i < cycleCount; i++) {
			int offset = reader.position();
			int posStepTenths = reader.readInt();
			long packed = reader.readUnsignedInt();

			int hours = (int) (packed & 0x1F);
			int minutes = (int) ((packed >>> 5) & 0x3F);
			int seconds = (int) ((packed >>> 11) & 0x3F);
			int cycleDurationTenthSec = (int) ((packed >>> 17) & 0x7F);
			int cycleNumber = (int) ((packed >>> 24) & 0x3);
			boolean rightPress = ((packed >>> 27) & 1) != 0;
			boolean alignmentEnabled = ((packed >>> 28) & 1) != 0;
			boolean leftLiftEnabled = ((packed >>> 29) & 1) != 0;
			boolean rightLiftEnabled = ((packed >>> 30) & 1) != 0;

			short deltaAlignment = reader.readShort();
			short deltaLeftLift = reader.readShort();
			short deltaRightLift = reader.readShort();

			int rawAlerts = reader.readUnsignedShort();
			boolean alertRegim = (rawAlerts & 1) != 0;
			boolean alertSkipTrack = ((rawAlerts >>> 1) & 1) != 0;
			boolean alertRollback = ((rawAlerts >>> 2) & 1) != 0;

			// Фильтрация мусорных циклов ПРУ по правилам DBN
			if ((long) (posStepTenths / 10) > headerStepCount || cycleDurationTenthSec == 0) {
				continue;
			}

			LocalTime recordedTime;
			try {
				recordedTime = LocalTime.of(hours, minutes, seconds);
			} catch (DateTimeException e) {
				throw new CrmParsingException("Invalid PRU cycle time at offset " + offset);
			}

			cycles.add(new CrmPruCycle(
					posStepTenths,
					recordedTime,
					cycleDurationTenthSec,
					cycleNumber,
					rightPress,
					alignmentEnabled,
					leftLiftEnabled,
					rightLiftEnabled,
					deltaAlignment,
					deltaLeftLift,
					deltaRightLift,
					alertRegim,
					alertSkipTrack,
					alertRollback
			));
		}

		return cycles;
	}

	private List<CrmRfid> parseRfids(
			BinaryReader reader,
			int version,
			int fragmentStepSizeMm,
			int rfidCount
	) {
		reader.requireRecordCount(rfidCount, RFID_SIZE, "CRM RFID");
		List<CrmRfid> rfids = new ArrayList<>(rfidCount);
		boolean legacyVersion = version < CrmHeaderParser.VERSION_LAYOUT_V6;

		for (int i = 0; i < rfidCount; i++) {
			long timestampMs = reader.readUnsignedInt();
			long syncPulseCount = reader.readUnsignedInt();
			long tagIdentifier = reader.readUnsignedInt();
			long dataBlock = reader.readUnsignedInt();
			float odometerDeltaLenM = reader.readFloat();
			float odometerScaleAdj = reader.readFloat();

			int state = reader.readUnsignedShort();
			int blockMask = state & 0xF;
			int pointCount = (state >>> 4) & 0x7FF;
			boolean hasError = (state & 0x8000) != 0;

			double controlDistanceM = reader.readDouble();

			// Пересчёт одометра и обнуление метки времени для старой версии < 0x0600
			if (legacyVersion) {
				odometerDeltaLenM = (float) ((double) timestampMs * fragmentStepSizeMm / 10000.0);
				timestampMs = 0L;
			}

			rfids.add(new CrmRfid(
					timestampMs,
					syncPulseCount,
					tagIdentifier,
					dataBlock,
					odometerDeltaLenM,
					odometerScaleAdj,
					blockMask,
					pointCount,
					hasError,
					controlDistanceM
			));
		}

		return rfids;
	}

	private List<CrmControlPoint> parseControlPoints(BinaryReader reader, int controlPointCount) {
		reader.requireRecordCount(controlPointCount, CTRL_POINT_SIZE, "control point");
		List<CrmControlPoint> controlPoints = new ArrayList<>(controlPointCount);

		for (int i = 0; i < controlPointCount; i++) {
			int offset = reader.position();
			long packed = reader.readUnsignedInt();

			int regimCode = (int) (packed & 0x1FFF);
			int tempCtrl = (int) ((packed >>> 13) & 0x3);
			int hours = (int) ((packed >>> 15) & 0x1F);
			int minutes = (int) ((packed >>> 20) & 0x3F);
			int seconds = (int) ((packed >>> 26) & 0x3F);

			LocalTime recordedTime;
			try {
				recordedTime = LocalTime.of(hours, minutes, seconds);
			} catch (DateTimeException e) {
				throw new CrmParsingException("Invalid control point time at offset " + offset);
			}

			short[] interfaceRegulators = new short[INTERFACE_REGULATOR_COUNT];
			for (int j = 0; j < INTERFACE_REGULATOR_COUNT; j++) {
				interfaceRegulators[j] = reader.readByte();
			}

			short[] externalRegulators = new short[EXTERNAL_REGULATOR_COUNT];
			for (int j = 0; j < EXTERNAL_REGULATOR_COUNT; j++) {
				externalRegulators[j] = reader.readByte();
			}

			short leftRailTempC = reader.readByte();
			short rightRailTempC = reader.readByte();
			short airTempC = reader.readByte();

			controlPoints.add(new CrmControlPoint(
					regimCode,
					tempCtrl,
					recordedTime,
					interfaceRegulators,
					externalRegulators,
					leftRailTempC,
					rightRailTempC,
					airTempC
			));
		}

		return controlPoints;
	}
}