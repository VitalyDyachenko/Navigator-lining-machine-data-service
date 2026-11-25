package ru.ccfit.nsu.navigator.dataservice.parser.common;

import ru.ccfit.nsu.navigator.dataservice.parser.common.model.PicketPosition;

public class PicketPositionParser {

	// Разбор структуры tsPKT_H
	public PicketPosition parse(BinaryReader reader) {
		long packedPosition = reader.readUnsignedInt();
		int picket = (int) (packedPosition & 0xF);
		int kilometer = (int) ((packedPosition >>> 4) & 0xFFFFFF);
		float meter = reader.readFloat();

		return new PicketPosition(kilometer, picket, meter);
	}
}