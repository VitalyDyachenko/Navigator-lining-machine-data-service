package ru.ccfit.nsu.navigator.dataservice.parser.common;

import ru.ccfit.nsu.navigator.dataservice.parser.common.model.MachineData;

public class MachineParser {

	// Разбор структуры tsHMASH
	public MachineData parse(BinaryReader reader) {
		int rawType = reader.readUnsignedByte();
		String name = reader.readFixedString(24);
		long number = reader.readUnsignedInt();

		// Геометрия рихтовочной системы
		int[] lr = new int[5];
		for (int i = 0; i < lr.length; i++) {
			lr[i] = reader.readUnsignedShort();
		}

		// Геометрия нивелировочной системы
		int[] ln = new int[5];
		for (int i = 0; i < ln.length; i++) {
			ln[i] = reader.readUnsignedShort();
		}

		// Геометрия тележек в миллиметрах
		int[] t = new int[4];
		for (int i = 0; i < t.length; i++) {
			t[i] = reader.readUnsignedShort();
		}

		// Ширина колеи
		int gaugeWidth = reader.readUnsignedShort();

		return new MachineData(
				rawType,
				(rawType >>> 5) & 0b111,
				name,
				number,
				((rawType >>> 4) & 1) != 0,
				((rawType >>> 3) & 1) != 0,
				((rawType >>> 2) & 1) != 0,
				rawType & 0b11,
				lr,
				ln,
				t,
				gaugeWidth
		);
	}
}