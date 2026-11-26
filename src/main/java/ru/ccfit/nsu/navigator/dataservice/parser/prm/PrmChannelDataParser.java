package ru.ccfit.nsu.navigator.dataservice.parser.prm;

import ru.ccfit.nsu.navigator.dataservice.exception.parsing.PrmParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.common.BinaryReader;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.ChannelInfo;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmHeader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class PrmChannelDataParser {

	private static final int CHANNEL_COUNT = 16;
	private static final int DEFAULT_VALUE_TYPE = 3;
	private static final int DEFAULT_PHYSICAL_TYPE = 1;
	private static final float DEFAULT_SCALE = 10.0f;

	// Чтение сырых байтов активных каналов согласно MaskCH и InfoCH[i].Val
	public Map<Integer, byte[]> parse(BinaryReader reader, PrmHeader header) {
		int mask = header.channelMask();
		long steps = header.stepCount();
		ChannelInfo[] channelInfos = header.channels();

		Map<Integer, byte[]> channels = new HashMap<>();

		for (int i = 0; i < CHANNEL_COUNT; i++) {
			if ((mask & (1 << i)) == 0) {
				continue;
			}

			ChannelInfo effectiveInfo = resolveEffectiveInfo(channelInfos[i]);
			int elementSize = valueSize(effectiveInfo.valueType());
			long bytesPerChannel = steps * elementSize;

			if (bytesPerChannel > Integer.MAX_VALUE) {
				throw new PrmParsingException("Channel data is too large: " + bytesPerChannel);
			}

			byte[] data = reader.readBytes((int) bytesPerChannel);
			channels.put(i, data);
		}

		return channels;
	}

	// Нормализация описания канала для старых форматов и нулевого масштаба
	public ChannelInfo resolveEffectiveInfo(ChannelInfo raw) {
		if (raw.valueType() == 0) {
			return new ChannelInfo(DEFAULT_VALUE_TYPE, DEFAULT_PHYSICAL_TYPE, DEFAULT_SCALE);
		}

		float scale = (raw.scale() == 0.0f) ? DEFAULT_SCALE : raw.scale();
		return new ChannelInfo(raw.valueType(), raw.physicalType(), scale);
	}

	// Определение размера одного отсчёта канала в байтах
	public int valueSize(int valueType) {
		return switch (valueType) {
			case 0 -> Short.BYTES;
			case 1, 2 -> Byte.BYTES;
			case 3, 4 -> Short.BYTES;
			case 5, 6, 7 -> Integer.BYTES;
			case 8, 9, 10 -> Long.BYTES;
			default -> throw new PrmParsingException("Unsupported channel value type: " + valueType);
		};
	}

	// Декодирование 16-битных знаковых отсчётов канала
	public short[] decodeShortValues(byte[] data) {
		if (data.length % Short.BYTES != 0) {
			throw new PrmParsingException("Invalid short channel data length: " + data.length);
		}

		ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
		short[] values = new short[data.length / Short.BYTES];

		for (int i = 0; i < values.length; i++) {
			values[i] = buffer.getShort();
		}

		return values;
	}

	// Декодирование физических значений канала с учётом типа отсчёта и Scale
	public double[] decodePhysicalValues(byte[] data, ChannelInfo rawInfo) {
		ChannelInfo info = resolveEffectiveInfo(rawInfo);
		int valueType = info.valueType();
		int elementSize = valueSize(valueType);

		if (data.length % elementSize != 0) {
			throw new PrmParsingException("Invalid channel data length: " + data.length);
		}

		int count = data.length / elementSize;
		ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
		double[] result = new double[count];
		double scale = info.scale();

		for (int i = 0; i < count; i++) {
			double rawValue = switch (valueType) {
				case 1 -> buffer.get();
				case 2 -> Byte.toUnsignedInt(buffer.get());
				case 3 -> buffer.getShort();
				case 4 -> Short.toUnsignedInt(buffer.getShort());
				case 5 -> buffer.getInt();
				case 6 -> Integer.toUnsignedLong(buffer.getInt());
				case 7 -> buffer.getFloat();
				case 8 -> buffer.getDouble();
				case 9 -> (double) buffer.getLong();
				case 10 -> decodeUnsignedLong(buffer.getLong());
				default -> throw new PrmParsingException("Unsupported channel value type: " + valueType);
			};

			result[i] = rawValue / scale;
		}

		return result;
	}

	private double decodeUnsignedLong(long value) {
		if (value >= 0) {
			return (double) value;
		}
		return (double) (value & Long.MAX_VALUE) + Math.pow(2.0, 63);
	}
}