package ru.ccfit.nsu.navigator.dataservice.parser.common;

import ru.ccfit.nsu.navigator.dataservice.parser.common.model.SurveyFlags;

public class SurveyFlagsParser {

	public SurveyFlags parse(BinaryReader reader) {
		return parse(reader.readUnsignedShort());
	}

	public SurveyFlags parse(int raw) {
		return new SurveyFlags(
				(raw & 1) != 0,
				((raw >>> 1) & 1) != 0,
				(raw >>> 2) & 0b11,
				((raw >>> 4) & 1) != 0,
				(raw >>> 5) & 0b11,
				((raw >>> 7) & 1) != 0,
				((raw >>> 8) & 1) != 0,
				(raw >>> 9) & 0x7F
		);
	}
}