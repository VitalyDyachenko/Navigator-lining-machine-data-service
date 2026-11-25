package ru.ccfit.nsu.navigator.dataservice.parser.common;

import ru.ccfit.nsu.navigator.dataservice.parser.common.model.ChannelInfo;

public class ChannelInfoParser {

	private static final int CHANNEL_COUNT = 16;

	// Разбор массива метаданных каналов tsINFO_CH
	public ChannelInfo[] parse(BinaryReader reader) {
		ChannelInfo[] channels = new ChannelInfo[CHANNEL_COUNT];

		for (int i = 0; i < CHANNEL_COUNT; i++) {
			int valueType = reader.readUnsignedByte();
			int physicalType = reader.readUnsignedByte();
			float scale = reader.readFloat();

			channels[i] = new ChannelInfo(valueType, physicalType, scale);
		}

		return channels;
	}
}