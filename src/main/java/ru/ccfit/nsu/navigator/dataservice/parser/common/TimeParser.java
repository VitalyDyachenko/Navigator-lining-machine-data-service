package ru.ccfit.nsu.navigator.dataservice.parser.common;

import java.time.DateTimeException;
import java.time.LocalDateTime;

public class TimeParser {

	// Разбор обязательной структуры tsHTIME
	public LocalDateTime parse(BinaryReader reader) {
		int offset = reader.position();
		long packedTime = reader.readUnsignedInt();
		return decode(reader, offset, packedTime);
	}

	// Разбор опциональной структуры tsHTIME с возвратом null при нулевом значении
	public LocalDateTime parseOptional(BinaryReader reader) {
		int offset = reader.position();
		long packedTime = reader.readUnsignedInt();

		if (packedTime == 0L) {
			return null;
		}

		return decode(reader, offset, packedTime);
	}

	private LocalDateTime decode(BinaryReader reader, int offset, long packedTime) {
		int year = (int) (packedTime & 0xFFF);
		int month = (int) ((packedTime >>> 12) & 0xF);
		int day = (int) ((packedTime >>> 16) & 0x1F);
		int hours = (int) ((packedTime >>> 21) & 0x1F);
		int minutes = (int) ((packedTime >>> 26) & 0x3F);

		try {
			return LocalDateTime.of(year, month, day, hours, minutes);
		} catch (DateTimeException e) {
			throw reader.fail("Invalid date-time at offset " + offset);
		}
	}
}